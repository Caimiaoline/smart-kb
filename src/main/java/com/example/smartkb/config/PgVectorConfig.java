package com.example.smartkb.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * PGVector 扩展初始化：确保数据库已启用 vector 类型。
 */
@Configuration
public class PgVectorConfig {

    private static final Logger log = LoggerFactory.getLogger(PgVectorConfig.class);

    private final JdbcTemplate jdbcTemplate;

    public PgVectorConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initVectorExtension() {
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            log.info("PGVector extension ready");
        } catch (Exception e) {
            log.warn("CREATE EXTENSION vector failed (may already exist or need superuser): {}", e.getMessage());
        }
    }
}
