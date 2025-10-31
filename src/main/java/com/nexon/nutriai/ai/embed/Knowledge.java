package com.nexon.nutriai.ai.embed;

import java.io.InputStream;
import java.net.URI;

public record Knowledge(String fileName, String filePath, URI uri, InputStream inputStream, FileType fileType) {

    public enum FileType {
        PDF, WORD, TXT
    }
}
