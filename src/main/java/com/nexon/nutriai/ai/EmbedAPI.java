package com.nexon.nutriai.ai;

import com.nexon.nutriai.ai.common.AiAPI;
import com.nexon.nutriai.pojo.request.AiEmbedRequest;
import org.springframework.ai.document.Document;

import java.util.List;

public interface EmbedAPI extends AiAPI {

    /**
     * 文本嵌入
     * @param aiEmbedRequest
     * @return
     */
    void embed(AiEmbedRequest aiEmbedRequest);

    /**
     * 检索
     * @param question
     * @return
     */
    List<Document> query(String question);
}
