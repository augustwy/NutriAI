package com.nexon.nutriai.ai.common;

public interface AiTool {

    String getName();

    String getDescription();

    Object getNativeTool(); // 返回具体大模型SDK所需的工具对象
}
