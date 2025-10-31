package com.nexon.nutriai.ai.embed;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 文本分块器，支持多种分块策略
 */
public class TextSplitter {

    /** 按字符数分块策略 */
    public static final String CHARACTER_BASED = "character_based";
    /** 按语义分块策略 */
    public static final String SEMANTIC_BASED = "semantic_based";
    /** 流式分块策略 */
    public static final String STREAMING_BASED = "streaming_based";

    private final ListSplittingStrategy listStrategy;
    private final SpliteratorSplittingStrategy spliteratorStrategy;
    private final int chunkSize;
    private final String strategyType;

    public TextSplitter(String strategyType, int chunkSize) {
        this.strategyType = strategyType;
        this.chunkSize = chunkSize;
        this.listStrategy = createListSplitter(strategyType);
        this.spliteratorStrategy = createSpliteratorSplitter(strategyType);
    }

    /**
     * 根据策略类型创建对应的分块策略(List<String>版本)
     */
    private ListSplittingStrategy createListSplitter(String type) {
        switch (type) {
            case CHARACTER_BASED:
                return new CharacterBasedSplittingStrategy();
            case SEMANTIC_BASED:
                return new SemanticBasedSplittingStrategy();
            case STREAMING_BASED:
                return new StreamingBasedSplittingStrategy();
            default:
                return new CharacterBasedSplittingStrategy();
        }
    }

    /**
     * 根据策略类型创建对应的分块策略(Spliterator<String>版本)
     */
    private SpliteratorSplittingStrategy createSpliteratorSplitter(String type) {
        switch (type) {
            case CHARACTER_BASED:
                return new CharacterBasedSplittingStrategy();
            case SEMANTIC_BASED:
                return new SemanticBasedSplittingStrategy();
            case STREAMING_BASED:
                return new StreamingBasedSplittingStrategy();
            default:
                return new CharacterBasedSplittingStrategy();
        }
    }

    /**
     * 对文本列表进行分块处理
     */
    public List<String> split(List<String> texts) {
        if (listStrategy == null) {
            throw new UnsupportedOperationException("Current strategy does not support List<String> input");
        }
        return listStrategy.split(texts, chunkSize);
    }

    /**
     * 对文本Spliterator进行分块处理
     */
    public Spliterator<String> split(Spliterator<String> lines) {
        if (spliteratorStrategy == null) {
            throw new UnsupportedOperationException("Current strategy does not support Spliterator<String> input");
        }
        return spliteratorStrategy.split(lines, chunkSize);
    }

    /**
     * 获取当前使用的策略类型
     */
    public String getStrategyType() {
        return strategyType;
    }

    /**
     * 分块策略接口 - 处理List<String>类型
     */
    private interface ListSplittingStrategy {
        List<String> split(List<String> texts, int chunkSize);
    }

    /**
     * 分块策略接口 - 处理Spliterator<String>类型
     */
    private interface SpliteratorSplittingStrategy {
        Spliterator<String> split(Spliterator<String> lines, int chunkSize);
    }

    /**
     * 基础分块策略实现
     */
    private static class CharacterBasedSplittingStrategy implements ListSplittingStrategy, SpliteratorSplittingStrategy {
        @Override
        public List<String> split(List<String> texts, int chunkSize) {
            List<String> chunks = new ArrayList<>();
            for (String text : texts) {
                chunks.addAll(splitText(text, chunkSize));
            }
            return chunks;
        }

        @Override
        public Spliterator<String> split(Spliterator<String> lines, int chunkSize) {
            return Spliterators.spliteratorUnknownSize(
                    new Iterator<String>() {
                        private Iterator<String> lineIterator = Spliterators.iterator(lines);
                        private String currentChunk = "";
                        private String nextLine;

                        @Override
                        public boolean hasNext() {
                            if (currentChunk.isEmpty()) {
                                if (lineIterator.hasNext()) {
                                    nextLine = lineIterator.next();
                                    currentChunk = nextLine;
                                    return true;
                                }
                                return false;
                            }
                            return !currentChunk.isEmpty();
                        }

                        @Override
                        public String next() {
                            if (!hasNext()) {
                                throw new NoSuchElementException();
                            }
                            String result = currentChunk;
                            currentChunk = "";
                            return result;
                        }
                    },
                    Spliterator.ORDERED | Spliterator.NONNULL
            );
        }

        /**
         * 按字符数分块单个字符串
         */
        private List<String> splitText(String text, int chunkSize) {
            List<String> result = new ArrayList<>();
            for (int i = 0; i < text.length(); i += chunkSize) {
                int end = Math.min(i + chunkSize, text.length());
                result.add(text.substring(i, end));
            }
            return result;
        }
    }

    /**
     * 语义分块策略实现
     */
    private static class SemanticBasedSplittingStrategy implements ListSplittingStrategy, SpliteratorSplittingStrategy {
        @Override
        public List<String> split(List<String> texts, int chunkSize) {
            List<String> chunks = new ArrayList<>();
            for (String text : texts) {
                chunks.addAll(splitByParagraphs(text, chunkSize));
            }
            return chunks;
        }

        @Override
        public Spliterator<String> split(Spliterator<String> lines, int chunkSize) {
            Iterator<String> iterator = Spliterators.iterator(lines);
            StringBuilder fullText = new StringBuilder();
            while (iterator.hasNext()) {
                fullText.append(iterator.next()).append("\n");
            }

            List<String> chunks = split(Collections.singletonList(fullText.toString()), chunkSize);
            return chunks.spliterator();
        }

        /**
         * 按段落分块
         */
        private List<String> splitByParagraphs(String text, int chunkSize) {
            // 按段落分割（以两个换行符为分隔）
            String[] paragraphs = text.split("\\n\\s*\\n");
            List<String> result = new ArrayList<>();

            for (String paragraph : paragraphs) {
                // 如果段落本身不超过chunkSize，则直接加入结果
                if (paragraph.length() <= chunkSize) {
                    result.add(paragraph);
                } else {
                    // 否则按字符数进一步分割
                    result.addAll(splitText(paragraph, chunkSize));
                }
            }
            return result;
        }

        /**
         * 按字符数分块单个字符串
         */
        private List<String> splitText(String text, int chunkSize) {
            List<String> result = new ArrayList<>();
            for (int i = 0; i < text.length(); i += chunkSize) {
                int end = Math.min(i + chunkSize, text.length());
                result.add(text.substring(i, end));
            }
            return result;
        }
    }

    /**
     * 流式分块策略实现
     */
    private static class StreamingBasedSplittingStrategy implements ListSplittingStrategy, SpliteratorSplittingStrategy {
        @Override
        public List<String> split(List<String> texts, int chunkSize) {
            // 将List<String>转换为单个字符串进行处理
            StringBuilder sb = new StringBuilder();
            for (String text : texts) {
                sb.append(text).append("\n");
            }
            List<String> result = new ArrayList<>();
            result.add(sb.toString());
            return result;
        }

        @Override
        public Spliterator<String> split(Spliterator<String> lines, int chunkSize) {
            return Spliterators.spliteratorUnknownSize(
                    new Iterator<String>() {
                        private Iterator<String> lineIterator = Spliterators.iterator(lines);
                        private String currentChunk = "";
                        private String nextLine;

                        @Override
                        public boolean hasNext() {
                            if (currentChunk.isEmpty()) {
                                if (lineIterator.hasNext()) {
                                    nextLine = lineIterator.next();
                                    currentChunk = nextLine;
                                    return true;
                                }
                                return false;
                            }
                            return !currentChunk.isEmpty();
                        }

                        @Override
                        public String next() {
                            if (!hasNext()) {
                                throw new NoSuchElementException();
                            }
                            String result = currentChunk;
                            currentChunk = "";
                            return result;
                        }
                    },
                    Spliterator.ORDERED | Spliterator.NONNULL
            );
        }
    }
}
