package com.nexon.nutriai.domain.valueobject;

import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.util.Objects;

@Getter
@Setter
public class UploadedFile {

    private String originalFilename;
    private String contentType;
    private InputStream inputStream;
    private byte[] content;

    public UploadedFile(String originalFilename, String contentType, InputStream inputStream) {
        this.originalFilename = Objects.requireNonNull(originalFilename);
        this.contentType = Objects.requireNonNull(contentType);
        this.inputStream = Objects.requireNonNull(inputStream);
    }

    public UploadedFile(String originalFilename, String contentType, byte[] content) {
        this.originalFilename = Objects.requireNonNull(originalFilename);
        this.contentType = Objects.requireNonNull(contentType);
        this.content = Objects.requireNonNull(content);
    }
}
