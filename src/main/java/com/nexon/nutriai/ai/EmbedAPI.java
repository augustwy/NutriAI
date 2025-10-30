package com.nexon.nutriai.ai;

import com.nexon.nutriai.ai.common.AiAPI;
import com.nexon.nutriai.pojo.request.AiEmbedRequest;

import java.util.List;

public interface EmbedAPI extends AiAPI {

    /**
     * 文本嵌入
     * @param aiEmbedRequest
     * @return
     */
    List<String> embed(AiEmbedRequest aiEmbedRequest);
}
