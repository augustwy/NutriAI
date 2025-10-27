package com.nexon.nutriai.pojo.request;

import lombok.Getter;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Getter
public class AiVisionRequest extends BaseAiRequest {

    private List<URI> uris;

    private List<String> filePaths;

    private List<InputStream> inputStreams;

    public AiVisionRequest() {
        super();
    }

    public AiVisionRequest(BaseRequest baseRequest) {
        super(baseRequest);
    }

    public AiVisionRequest(BaseAiRequest baseAiRequest) {
        super(baseAiRequest);
        this.setContent(baseAiRequest.getContent());
        this.setSystemPrompt(baseAiRequest.getSystemPrompt());
        this.setContext(baseAiRequest.getContext());
    }

    public void add(URI uri) {
        if (uris == null) {
            uris = new ArrayList<>();
        }
        uris.add(uri);
    }

    public void add(String filePath) {
        if (filePaths == null) {
            filePaths = new ArrayList<>();
        }
        filePaths.add(filePath);
    }

    public void add(InputStream inputStream) {
        if (inputStreams == null) {
            inputStreams = new ArrayList<>();
        }
        inputStreams.add(inputStream);
    }
}
