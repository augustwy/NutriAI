package com.nexon.nutriai.pojo.request;

import com.nexon.nutriai.ai.embed.Knowledge;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class AiEmbedRequest extends BaseRequest {

    private List<Knowledge> knowledges;

    private String addModel = "append";

    public AiEmbedRequest(List<Knowledge> knowledges) {
        super();
        this.knowledges = knowledges;
    }

    public String getAddModel() {
        return null != addModel ? addModel : "append";
    }
}
