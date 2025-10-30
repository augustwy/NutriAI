package com.nexon.nutriai.config.lancedb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(name = "lancedb.python.enable", havingValue = "true")
public class LanceDBWebFluxInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final ReactiveLanceDBProcessManager processManager;

    public LanceDBWebFluxInitializer(ReactiveLanceDBProcessManager processManager) {
        this.processManager = processManager;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 订阅 Mono，启动进程。使用 subscribe() 是非阻塞的。
        processManager.startPythonServer()
                .doOnSuccess(_ -> log.info("LanceDB Python service startup sequence initiated."))
                .doOnError(e -> log.error("LanceDB Python service failed to start.", e))
                .subscribe();
    }
}

