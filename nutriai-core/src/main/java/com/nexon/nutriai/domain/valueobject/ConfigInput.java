package com.nexon.nutriai.domain.valueobject;

import java.util.Map;

/**
 * 表示动态配置的输入值对象。
 * 使用 Map<String, Object> 是一个与框架无关的、表示动态结构的好方法。
 */
public class ConfigInput {
    private final Map<String, Object> data;

    public ConfigInput(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getData() {
        return data;
    }
}