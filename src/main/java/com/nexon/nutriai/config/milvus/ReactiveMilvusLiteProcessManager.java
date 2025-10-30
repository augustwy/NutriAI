package com.nexon.nutriai.config.milvus;

import com.nexon.nutriai.config.properties.MilvusLiteProperties;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ReactiveMilvusLiteProcessManager {

    private static final Logger log = LoggerFactory.getLogger(ReactiveMilvusLiteProcessManager.class);
    private final AtomicReference<Process> pythonProcess = new AtomicReference<>();
    private final MilvusLiteProperties properties;

    public ReactiveMilvusLiteProcessManager(MilvusLiteProperties properties) {
        this.properties = properties;
    }

    public void start() {
        Mono.fromRunnable(() -> {
                    String scriptPath = properties.getScriptPath();
                    // 如果路径不是以"scripts/"开头，则加上前缀
                    if (!scriptPath.startsWith("scripts/")) {
                        scriptPath = "scripts/" + scriptPath;
                    }

                    InputStream scriptStream = getClass().getClassLoader().getResourceAsStream(scriptPath);

                    if (scriptStream == null) {
                        throw new RuntimeException("Python script not found in classpath: " + scriptPath);
                    }

                    // 创建临时文件来运行脚本
                    Path tempScriptFile = null;
                    try {
                        tempScriptFile = Files.createTempFile("milvus_lite_server", ".py");
                        Files.copy(scriptStream, tempScriptFile, StandardCopyOption.REPLACE_EXISTING);
                        // 设置文件权限
                        tempScriptFile.toFile().setExecutable(true);
                        tempScriptFile.toFile().setReadable(true);
                        tempScriptFile.toFile().setWritable(true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    // 确保临时文件在JVM退出时被删除
                    tempScriptFile.toFile().deleteOnExit();

                    // 先检查Python环境
                    checkPythonEnvironment();

                    List<String> command = new ArrayList<>();
                    // 尝试先用 python，再用 python3
                    command.add("python"); // 优先使用 python
                    command.add(tempScriptFile.toString());
                    command.add("--port");
                    command.add(String.valueOf(properties.getPort()));
                    command.add("--data-dir");
                    command.add(properties.getDataDir());

                    ProcessBuilder processBuilder = new ProcessBuilder(command);
                    // 设置PYTHONIOENCODING环境变量以支持UTF-8编码
                    processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
                    processBuilder.redirectErrorStream(true);

                    // 设置工作目录为项目根目录
                    processBuilder.directory(new File("."));

                    log.info("Starting Milvus Lite with command: {}", String.join(" ", command));
                    Process process = null;
                    try {
                        process = processBuilder.start();
                    } catch (IOException e) {
                        log.error("Failed to start process with command: {}", String.join(" ", command), e);
                        // 如果 python 不可用，尝试使用 python3
                        try {
                            command.set(0, "python3");
                            log.info("Retrying with python3 command: {}", String.join(" ", command));
                            processBuilder = new ProcessBuilder(command);
                            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
                            processBuilder.redirectErrorStream(true);
                            process = processBuilder.start();
                        } catch (IOException ioException) {
                            log.error("Failed to start process with python3 command: {}", String.join(" ", command), ioException);
                            throw new RuntimeException("Failed to start Milvus Lite process with both python and python3", e);
                        }
                    }
                    pythonProcess.set(process);

                    Process finalProcess = process;
                    final AtomicBoolean startupConfirmed = new AtomicBoolean(false);

                    new Thread(() -> {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(finalProcess.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                log.info("[Milvus Lite]: {}", line);

                                // 检查是否启动成功
                                if (line.contains("Milvus Lite started successfully")) {
                                    log.info("Milvus Lite server startup confirmed");
                                    startupConfirmed.set(true);
                                }

                                // 检查是否启动失败
                                if (line.contains("SERVER_START_FAILED")) {
                                    log.error("Milvus Lite server failed to start");
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            log.error("Error reading Milvus Lite process output", e);
                        }
                    }).start();

                    // 等待一段时间，确认进程启动成功
                    try {
                        Thread.sleep(30000); // 增加到30秒
                        if (!process.isAlive()) {
                            log.error("Milvus Lite process exited prematurely with exit code: {}", process.exitValue());
                            throw new RuntimeException("Milvueue Lite process failed to start");
                        } else {
                            log.info("Milvus Lite process is running");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while waiting for Milvus Lite to start", e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private void checkPythonEnvironment() {
        log.debug("Checking Python environment...");
        
        // 检查 python3 是否可用
        try {
            Process process = new ProcessBuilder("python3", "--version").start();
            process.waitFor();
            if (process.exitValue() == 0) {
                log.debug("python3 is available");
            } else {
                log.warn("python3 is not available");
            }
        } catch (Exception e) {
            log.warn("Failed to check python3 availability: {}", e.getMessage());
        }
        
        // 检查 python 是否可用
        try {
            Process process = new ProcessBuilder("python", "--version").start();
            process.waitFor();
            if (process.exitValue() == 0) {
                log.debug("python is available");
            } else {
                log.warn("python is not available");
            }
        } catch (Exception e) {
            log.warn("Failed to check python availability: {}", e.getMessage());
        }
    }

    public Mono<Void> stop() {
        return Mono.fromRunnable(() -> {
                    Process process = pythonProcess.get();
                    if (process != null && process.isAlive()) {
                        log.info("Stopping Milvus Lite process...");
                        process.destroy();
                        try {
                            if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                                log.warn("Milvus Lite process did not terminate gracefully, force killing...");
                                process.destroyForcibly();
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            process.destroyForcibly();
                        }
                        log.info("Milvus Lite process stopped.");
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @PreDestroy
    public void onShutdown() {
        stop().block();
    }
}
