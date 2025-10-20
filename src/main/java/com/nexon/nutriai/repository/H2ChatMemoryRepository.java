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
        return String.format("SELECT name FROM sqlite_master WHERE type = 'table' AND name LIKE '%s'", tableName);
    }

    protected String createTableSql(String tableName) {
        return String.format("CREATE TABLE IF NOT EXISTS %s ( conversation_id TEXT NOT NULL,    content TEXT NOT NULL, type TEXT NOT NULL, timestamp REAL NOT NULL,    CHECK (type IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL')));", tableName);
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