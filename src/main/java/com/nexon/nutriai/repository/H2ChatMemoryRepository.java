package com.nexon.nutriai.repository;

import com.alibaba.cloud.ai.memory.jdbc.JdbcChatMemoryRepository;
import org.springframework.jdbc.core.JdbcTemplate;

public class H2ChatMemoryRepository extends JdbcChatMemoryRepository {
    private H2ChatMemoryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public static H2Builder h2Builder() {
        return new H2Builder();
    }

    protected String hasTableSql(String tableName) {
        return String.format("SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '%s' FETCH FIRST 1 ROWS ONLY", tableName.toUpperCase());
    }

    protected String createTableSql(String tableName) {
        return String.format("CREATE TABLE IF NOT EXISTS %s (id BIGINT AUTO_INCREMENT PRIMARY KEY, conversation_id VARCHAR(256) NOT NULL, content LONGTEXT NOT NULL, type VARCHAR(100) NOT NULL, timestamp TIMESTAMP NOT NULL, CONSTRAINT chk_message_type CHECK (type IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL')))", tableName);
    }

    protected String getAddSql() {
        return "INSERT INTO ai_chat_memory (conversation_id, content, type, timestamp) VALUES (?, ?, ?, ?)";
    }

    protected String getGetSql() {
        return "SELECT content, type FROM ai_chat_memory WHERE conversation_id = ? ORDER BY timestamp";
    }

    public static class H2Builder {
        private JdbcTemplate jdbcTemplate;

        public H2Builder jdbcTemplate(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
            return this;
        }

        public H2ChatMemoryRepository build() {
            return new H2ChatMemoryRepository(this.jdbcTemplate);
        }
    }
}