package com.nexon.nutriai.ai.common;

import com.nexon.nutriai.ai.ChatAPI;
import com.nexon.nutriai.ai.TextAPI;
import com.nexon.nutriai.ai.VisionAPI;

public interface AiServiceFactory {

    /**
     * 创建聊天服务
     * @param provider
     * @param model
     * @return
     */
    ChatAPI createChatService(String provider, String model);

    /**
     * 创建视觉服务
     * @param provider
     * @param model
     * @return
     */
    VisionAPI createVisionService(String provider, String model);

    /**
     * 创建文本服务
     * @param provider
     * @param model
     * @return
     */
    TextAPI createTextService(String provider, String model);
}
