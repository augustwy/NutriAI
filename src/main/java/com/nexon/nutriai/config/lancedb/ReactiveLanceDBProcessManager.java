package com.nexon.nutriai.config.lancedb;

import com.nexon.nutriai.config.properties.LanceDBProperties;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class ReactiveLanceDBProcessManager {

    private static final Logger log = LoggerFactory.getLogger(ReactiveLanceDBProcessManager.class);
    private final AtomicReference<Process> pythonProcess = new AtomicReference<>();

    private final LanceDBProperties lanceDBProperties;

    public Mono<Void> startPythonServer() {
        return Mono.fromCallable(() -> {
                    // 这个 Callable 中的代码是阻塞的，会在指定的调度器上执行
                    // 从classpath中获取资源文件
                    String scriptPath = lanceDBProperties.getScriptPath();
                    // 如果路径不是以"scripts/"开头，则加上前缀
                    if (!scriptPath.startsWith("scripts/")) {
                        scriptPath = "scripts/" + scriptPath;
                    }

                    InputStream scriptStream = getClass().getClassLoader().getResourceAsStream(scriptPath);

                    if (scriptStream == null) {
                        throw new RuntimeException("Python script not found in classpath: " + scriptPath);
                    }

                    // 创建临时文件来运行脚本
                    Path tempScriptFile = Files.createTempFile("lancedb_server", ".py");
                    Files.copy(scriptStream, tempScriptFile, StandardCopyOption.REPLACE_EXISTING);

                    // 确保临时文件在JVM退出时被删除
                    tempScriptFile.toFile().deleteOnExit();

                    List<String> command = new ArrayList<>();
                    command.add("python");
                    command.add(tempScriptFile.toString());
                    command.add("--host");
                    command.add(lanceDBProperties.getHost());
                    command.add("--port");
                    command.add(String.valueOf(lanceDBProperties.getPort()));

                    ProcessBuilder processBuilder = new ProcessBuilder(command);
                    processBuilder.environment().put("LANCEDB_URI", lanceDBProperties.getDbUri());
                    // 设置PYTHONIOENCODING环境变量以支持UTF-8编码
                    processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
                    processBuilder.redirectErrorStream(true);

                    log.info("Starting LanceDB Python server with command: {}", command);
                    Process process = processBuilder.start();
                    pythonProcess.set(process); // 保存进程引用

                    // 在一个新线程中读取输出，防止阻塞
                    new Thread(() -> {
                        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                log.info("[LanceDB Python]: {}", line);
                            }
                        } catch (java.io.IOException e) {
                            log.error("Error reading Python process output", e);
                        }
                    }).start();

                    return (Void) null;
                })
                // 关键：将阻塞操作订阅到 boundedElastic 调度器线程池
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> log.info("LanceDB Python server start command issued."))
                .doOnError(e -> log.error("Failed to issue start command for LanceDB Python server.", e))
                .then(); // 转换为 Mono<Void>
    }

    public Mono<Void> stopPythonServer() {
        return Mono.fromRunnable(() -> {
                    Process process = pythonProcess.get();
                    if (process != null && process.isAlive()) {
                        log.info("Stopping LanceDB Python server...");
                        process.destroy();
                        try {
                            if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                                log.warn("Python server did not terminate gracefully, forcing kill.");
                                process.destroyForcibly();
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            process.destroyForcibly();
                        }
                        log.info("LanceDB Python server stopped.");
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @PreDestroy
    public void onShutdown() {
        // 在应用关闭时，同步地确保进程被停止
        stopPythonServer().block();
    }
}
