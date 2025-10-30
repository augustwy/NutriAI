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
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    // 确保临时文件在JVM退出时被删除
                    tempScriptFile.toFile().deleteOnExit();

                    List<String> command = new ArrayList<>();
                    command.add("python");
                    command.add(tempScriptFile.toString());
                    command.add("--port");
                    command.add(String.valueOf(properties.getPort()));
                    command.add("--data-dir");
                    command.add(properties.getDataDir());

                    ProcessBuilder processBuilder = new ProcessBuilder(command);
                    // 设置PYTHONIOENCODING环境变量以支持UTF-8编码
                    processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
                    processBuilder.redirectErrorStream(true);

                    log.info("Starting Milvus Lite with command: {}", command);
                    Process process = null;
                    try {
                        process = processBuilder.start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    pythonProcess.set(process);

                    Process finalProcess = process;
                    new Thread(() -> {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(finalProcess.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                log.info("[Milvus Lite]: {}", line);
                            }
                        } catch (Exception e) {
                            log.error("Error reading Milvus Lite process output", e);
                        }
                    }).start();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public Mono<Void> stop() {
        return Mono.fromRunnable(() -> {
                    Process process = pythonProcess.get();
                    if (process != null && process.isAlive()) {
                        log.info("Stopping Milvus Lite process...");
                        process.destroy();
                        try {
                            if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
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
