package com.nexon.nutriai.ai.embed;

import lombok.Getter;

import java.io.InputStream;
import java.net.URI;

@Getter
public class Knowledge {
    private String fileName;
    private String filePath;
    private URI uri;
    private InputStream inputStream;
    private FileType fileType;

    public Knowledge(String fileName, String filePath, FileType fileType) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
    }

    public Knowledge(String fileName, URI uri, FileType fileType) {
        this.fileName = fileName;
        this.uri = uri;
        this.fileType = fileType;
    }

    public Knowledge(String fileName, InputStream inputStream, FileType fileType) {
        this.fileName = fileName;
        this.inputStream = inputStream;
        this.fileType = fileType;
    }

    public enum FileType {
        PDF,
        WORD,
        TXT,
        ;

        public static FileType getFileType(String fileType) {
            for (FileType type : FileType.values()) {
                if (type.name().equals(fileType)) {
                    return type;
                }
            }
            return null;
        }
    }
}
