package com.nexon.nutriai.ai.embed;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * 文档读取器，支持多种文档格式
 */
public class DocumentReader {

    private final Knowledge knowledge;

    public DocumentReader(Knowledge knowledge) {
        if (knowledge == null) {
            throw new IllegalArgumentException("knowledge 不能为 null");
        }
        this.knowledge = knowledge;
    }

    /**
     * 读取文档内容，返回行的Spliterator
     * @return Spliterator<String> 行迭代器
     */

    public Spliterator<String> read() {
        // 根据输入源选择读取策略
        SourceReadingStrategy sourceStrategy = SourceReadingStrategyFactory.createStrategy(knowledge);
        return sourceStrategy.read(knowledge);
    }

    /**
     * 输入源读取策略接口
     */
    private interface SourceReadingStrategy {
        Spliterator<String> read(Knowledge knowledge);
    }

    /**
     * 输入源读取策略工厂
     */
    private static class SourceReadingStrategyFactory {
        public static SourceReadingStrategy createStrategy(Knowledge knowledge) {
            if (knowledge.inputStream() != null) {
                return new StreamSourceReadingStrategy();
            } else if (knowledge.filePath() != null) {
                return new FileSourceReadingStrategy();
            } else if (knowledge.uri() != null) {
                return new UriSourceReadingStrategy();
            } else {
                throw new IllegalStateException("未提供有效的输入源（inputStream、filePath 或 uri）");
            }
        }
    }

    /**
     * InputStream输入源读取策略
     */
    private static class StreamSourceReadingStrategy implements SourceReadingStrategy {
        @Override
        public Spliterator<String> read(Knowledge knowledge) {
            FileTypeProcessingStrategy fileStrategy = FileTypeProcessingStrategyFactory.createStrategy(knowledge);
            return fileStrategy.process(knowledge.inputStream());
        }
    }

    /**
     * 文件输入源读取策略
     */
    private static class FileSourceReadingStrategy implements SourceReadingStrategy {
        @Override
        public Spliterator<String> read(Knowledge knowledge) {
            try (InputStream inputStream = new FileInputStream(knowledge.filePath())) {
                FileTypeProcessingStrategy fileStrategy = FileTypeProcessingStrategyFactory.createStrategy(knowledge);
                return fileStrategy.process(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("无法读取文件: " + knowledge.filePath(), e);
            }
        }
    }

    /**
     * URI输入源读取策略
     */
    private static class UriSourceReadingStrategy implements SourceReadingStrategy {
        @Override
        public Spliterator<String> read(Knowledge knowledge) {
            URI uri = knowledge.uri();
            if (uri.getScheme().equals("file")) {
                try (InputStream inputStream = new FileInputStream(uri.getPath())) {
                    FileTypeProcessingStrategy fileStrategy = FileTypeProcessingStrategyFactory.createStrategy(knowledge);
                    return fileStrategy.process(inputStream);
                } catch (IOException e) {
                    throw new RuntimeException("无法读取文件: " + uri, e);
                }
            } else if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
                try {
                    URL url = uri.toURL();
                    try (InputStream inputStream = url.openStream()) {
                        FileTypeProcessingStrategy fileStrategy = FileTypeProcessingStrategyFactory.createStrategy(knowledge);
                        return fileStrategy.process(inputStream);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("无法打开远程文件: " + uri, e);
                }
            } else {
                throw new IllegalArgumentException("不支持的 URI 协议: " + uri.getScheme());
            }
        }
    }

    /**
     * 文件类型处理策略接口
     */
    private interface FileTypeProcessingStrategy {
        Spliterator<String> process(InputStream inputStream);
    }

    /**
     * 文件类型处理策略工厂
     */
    private static class FileTypeProcessingStrategyFactory {
        public static FileTypeProcessingStrategy createStrategy(Knowledge knowledge) {
            return switch (knowledge.fileType()) {
                case PDF -> new PdfFileTypeProcessingStrategy();
                case WORD -> new WordFileTypeProcessingStrategy();
                default ->
                    // 默认按文本处理
                        new TextFileTypeProcessingStrategy();
            };
        }
    }

    /**
     * PDF文件类型处理策略
     */
    private static class PdfFileTypeProcessingStrategy implements FileTypeProcessingStrategy {
        @Override
        public Spliterator<String> process(InputStream inputStream) {
            try (PDDocument document = PDDocument.load(inputStream)) {
                return new PDFPageSpliterator(document);
            } catch (IOException e) {
                throw new RuntimeException("无法解析PDF文件", e);
            }
        }
    }

    /**
     * Word文件类型处理策略
     */
    private static class WordFileTypeProcessingStrategy implements FileTypeProcessingStrategy {
        @Override
        public Spliterator<String> process(InputStream inputStream) {
            try (XWPFDocument document = new XWPFDocument(inputStream)) {
                // 从文档中一次性获取所有段落的列表
                var paragraphs = document.getParagraphs();
                // 创建一个顺序流，并在流关闭时关闭文档
                return new WordParagraphSpliterator(paragraphs);
            } catch (IOException e) {
                throw new RuntimeException("无法解析Word文件", e);
            }
        }
    }

    /**
     * 文本文件类型处理策略
     */
    private static class TextFileTypeProcessingStrategy implements FileTypeProcessingStrategy {
        @Override
        public Spliterator<String> process(InputStream inputStream) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return Spliterators.spliteratorUnknownSize(
                        new Iterator<String>() {
                            private String nextLine;

                            @Override
                            public boolean hasNext() {
                                try {
                                    nextLine = reader.readLine();
                                    return nextLine != null;
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            @Override
                            public String next() {
                                if (!hasNext()) {
                                    throw new NoSuchElementException();
                                }
                                return nextLine != null ? nextLine.trim() : null;
                            }
                        },
                        Spliterator.ORDERED | Spliterator.NONNULL
                );
            } catch (IOException e) {
                throw new RuntimeException("无法读取文本文件", e);
            }
        }
    }

    private static class PDFPageSpliterator implements Spliterator<String> {
        private final PDDocument document;
        private final PDFTextStripper stripper;
        private int currentPage;
        private int endPage;

        public PDFPageSpliterator(PDDocument document) throws IOException {
            this.document = document;
            this.stripper = new PDFTextStripper();
            this.currentPage = 1;
            this.endPage = document.getNumberOfPages();
        }

        @Override
        public boolean tryAdvance(Consumer<? super String> action) {
            if (currentPage <= endPage) {
                try {
                    stripper.setStartPage(currentPage);
                    stripper.setEndPage(currentPage);
                    String pageText = stripper.getText(document);
                    action.accept(pageText);
                    currentPage++;
                    return true;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return false;
        }

        @Override
        public Spliterator<String> trySplit() {
            int remaining = endPage - currentPage + 1;
            if (remaining <= 1) return null;
            int mid = currentPage + remaining / 2;
            try {
                PDFPageSpliterator newSpliterator = new PDFPageSpliterator(document);
                newSpliterator.currentPage = currentPage;
                newSpliterator.endPage = mid - 1;
                this.currentPage = mid;
                return newSpliterator;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public long estimateSize() {
            return endPage - currentPage + 1;
        }

        @Override
        public int characteristics() {
            return ORDERED | SIZED | SUBSIZED | NONNULL;
        }
    }

    private static class WordParagraphSpliterator implements Spliterator<String> {
        private final List<XWPFParagraph> paragraphs;
        private int current; // 当前迭代到的索引
        private final int end; // 迭代的结束索引（不包含）

        /**
         * 构造函数，接收一个段落列表和迭代范围
         * @param paragraphs 段落列表
         * @param start 起始索引（包含）
         * @param end 结束索引（不包含）
         */
        public WordParagraphSpliterator(List<XWPFParagraph> paragraphs, int start, int end) {
            this.paragraphs = paragraphs;
            this.current = start;
            this.end = end;
        }

        /**
         * 便利构造函数，用于迭代整个列表
         * @param paragraphs 段落列表
         */
        public WordParagraphSpliterator(List<XWPFParagraph> paragraphs) {
            this(paragraphs, 0, paragraphs.size());
        }

        @Override
        public boolean tryAdvance(Consumer<? super String> action) {
            if (current < end) {
                String text = paragraphs.get(current).getText();
                action.accept(text);
                current++;
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<String> trySplit() {
            int remaining = end - current;
            if (remaining <= 1) {
                return null; // 剩余元素太少，不再分割
            }

            // 计算分割点
            int mid = current + remaining / 2;

            // 创建一个新的 Spliterator，负责处理 [current, mid) 范围
            Spliterator<String> newSpliterator = new WordParagraphSpliterator(paragraphs, current, mid);

            // 当前 Spliterator 更新为负责处理 [mid, end) 范围
            this.current = mid;

            return newSpliterator;
        }

        @Override
        public long estimateSize() {
            return end - current;
        }

        @Override
        public int characteristics() {
            // ORDERED: 段落有顺序
            // SIZED: estimateSize() 返回精确大小
            // SUBSIZED: 分割后的子 Spliterator 也是 SIZED 的
            // NONNULL: 不会返回 null
            return ORDERED | SIZED | SUBSIZED | NONNULL;
        }
    }
}
