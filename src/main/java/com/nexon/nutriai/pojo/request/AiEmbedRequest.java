package com.nexon.nutriai.pojo.request;

import lombok.Getter;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Getter
public class AiEmbedRequest extends BaseRequest {

    private List<URI> uris;

    private List<String> filePaths;

    private List<InputStream> inputStreams;

    private String fileType;

    private String addModel = "append";

    public AiEmbedRequest() {
        super();
    }

    public AiEmbedRequest(String fileType) {
        super();
        this.fileType = fileType;
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

    public void setAddModel(String addModel) {
        this.addModel = addModel;
    }

    public String getAddModel() {
        return null != addModel ? addModel : "append";
    }
}
