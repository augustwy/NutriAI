package com.nexon.nutriai.domain.valueobject;

import java.time.Instant;
import java.util.Map;

/**
 * 表示配置快照的输出值对象。
 */
public class ConfigSnapshot {
    private final Map<String, Object> configData;
    private final Instant lastUpdated;

    public ConfigSnapshot(Map<String, Object> configData, Instant lastUpdated) {
        this.configData = configData;
        this.lastUpdated = lastUpdated;
    }

    // Getters...
    public Map<String, Object> getConfigData() { return configData; }
    public Instant getLastUpdated() { return lastUpdated; }
}