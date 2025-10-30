package com.nexon.nutriai.config.milvus;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(name = "milvus.python.enable", havingValue = "true", matchIfMissing = false)
public class MilvusLiteWebFluxInitializer implements ApplicationListener<ApplicationReadyEvent>  {

    private final ReactiveMilvusLiteProcessManager reactiveMilvusLiteProcessManager;

    public MilvusLiteWebFluxInitializer(ReactiveMilvusLiteProcessManager reactiveMilvusLiteProcessManager) {
        this.reactiveMilvusLiteProcessManager = reactiveMilvusLiteProcessManager;
    }
    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        reactiveMilvusLiteProcessManager.start();
    }
}
