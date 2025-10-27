package com.nexon.nutriai.ai.common;

public class AiToolUtil {

    public static AiTool build(Object tool, String description, String name) {
        return new AiTool() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public Object getNativeTool() {
                return tool;
            }
        };
    }
}
