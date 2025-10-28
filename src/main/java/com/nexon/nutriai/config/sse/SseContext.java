package com.nexon.nutriai.config.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBufferFactory;

/**
 * SSE 转换上下文，封装了转换过程中可能需要的所有信息。
 */
public class SseContext {
    private final String chatId;
    private final String model;
    private final String contentType;
    private final DataBufferFactory bufferFactory;
    private final ObjectMapper objectMapper;

    // 构造器、Getter...
    public SseContext(String chatId, String model, String contentType, DataBufferFactory bufferFactory, ObjectMapper objectMapper) {
        this.chatId = chatId;
        this.model = model;
        this.contentType = contentType;
        this.bufferFactory = bufferFactory;
        this.objectMapper = objectMapper;
    }

    public String getChatId() { return null != chatId ? chatId : ""; }
    public String getModel() { return null != model ? model : ""; }
    public String getContentType() { return null != contentType ? contentType : ""; }
    public DataBufferFactory getBufferFactory() { return bufferFactory; }
    public ObjectMapper getObjectMapper() { return objectMapper; }
}