package com.tripjoy.api.configuration;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Creates advanced PostgreSQL indexes that cannot be defined
 * through JPA @Index annotations (GIN, GiST, partial indexes, etc.).
 *
 * These indexes are created AFTER Hibernate has finished creating/updating
 * the schema (ddl-auto: update), using ApplicationReadyEvent to ensure
 * all tables exist before index creation.
 *
 * Standard B-tree indexes remain in JPA @Table(indexes = {...}) annotations
 * on their respective entity classes.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DatabaseIndexConfig {

    JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void createAdvancedIndexes() {
        log.info("Creating advanced PostgreSQL indexes (GIN/GiST)...");

        // ── Full-Text Search index on chat_message.message_content ──
        // Uses 'simple' tsvector config (language-agnostic, works with Vietnamese + English)
        jdbcTemplate.execute(
                """
				CREATE INDEX IF NOT EXISTS idx_chat_message_content_fts
					ON chat_message
					USING GIN (to_tsvector('simple', coalesce(message_content, '')))
				""");

        // ── Trigram index for ILIKE fuzzy search (pg_trgm) ──
        // Enables fast ILIKE '%keyword%' queries as FTS fallback
        jdbcTemplate.execute(
                """
				CREATE INDEX IF NOT EXISTS idx_chat_message_content_trgm
					ON chat_message
					USING GIN (message_content gin_trgm_ops)
				""");

        // ── Post Full-Text Search index ──
        jdbcTemplate.execute(
                """
				CREATE INDEX IF NOT EXISTS idx_post_content_fts
					ON post
					USING GIN (to_tsvector('simple', coalesce(content, '')))
				""");

        jdbcTemplate.execute(
                """
				CREATE INDEX IF NOT EXISTS idx_post_content_trgm
					ON post
					USING GIN (content gin_trgm_ops)
				""");

        log.info("Advanced PostgreSQL indexes created successfully.");
    }
}
