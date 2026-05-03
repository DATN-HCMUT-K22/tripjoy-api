CREATE TABLE system_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value TEXT NOT NULL,
    data_type VARCHAR(20) NOT NULL, -- STRING, NUMBER, BOOLEAN, JSON
    config_group VARCHAR(50) NOT NULL, -- AI, CHAT, MEDIA, SYSTEM
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

-- Seed initial data for AI Service
INSERT INTO system_config (config_key, config_value, data_type, config_group, description) VALUES
('AI_TIMEOUT_SECONDS', '120', 'NUMBER', 'AI', 'Timeout for AI service requests in seconds'),
('AI_CONNECT_TIMEOUT_SECONDS', '10', 'NUMBER', 'AI', 'Connection timeout for AI service in seconds'),
('AI_CB_SLIDING_WINDOW_SIZE', '5', 'NUMBER', 'AI', 'Circuit Breaker sliding window size'),
('AI_CB_MIN_CALLS', '3', 'NUMBER', 'AI', 'Circuit Breaker minimum number of calls before calculating failure rate'),
('AI_CB_FAILURE_RATE_THRESHOLD', '50', 'NUMBER', 'AI', 'Circuit Breaker failure rate threshold percentage'),
('AI_RETRY_MAX_ATTEMPTS', '3', 'NUMBER', 'AI', 'Maximum number of retry attempts for AI requests'),
('AI_RETRY_WAIT_DURATION', '2', 'NUMBER', 'AI', 'Wait duration between retries in seconds');

-- Seed initial data for Chat Service
INSERT INTO system_config (config_key, config_value, data_type, config_group, description) VALUES
('CHAT_MSG_RATE_LIMIT', '10', 'NUMBER', 'CHAT', 'Maximum messages per second per user'),
('CHAT_TYPING_RATE_LIMIT', '1', 'NUMBER', 'CHAT', 'Maximum typing events per second per user'),
('CHAT_MAX_PINNED_MESSAGES', '50', 'NUMBER', 'CHAT', 'Maximum pinned messages allowed in a conversation');

-- Seed initial data for Media Service
INSERT INTO system_config (config_key, config_value, data_type, config_group, description) VALUES
('MEDIA_MAX_IMAGE_SIZE_MB', '10', 'NUMBER', 'MEDIA', 'Maximum image upload size in megabytes'),
('MEDIA_MAX_VIDEO_SIZE_MB', '50', 'NUMBER', 'MEDIA', 'Maximum video upload size in megabytes');

-- Seed initial data for System
INSERT INTO system_config (config_key, config_value, data_type, config_group, description) VALUES
('SYSTEM_DEFAULT_SEARCH_RADIUS', '5000', 'NUMBER', 'SYSTEM', 'Default search radius for locations in meters'),
('SYSTEM_MAX_SEARCH_RADIUS', '50000', 'NUMBER', 'SYSTEM', 'Maximum allowed search radius in meters'),
('SYSTEM_MAX_PAGE_SIZE', '200', 'NUMBER', 'SYSTEM', 'Maximum results per page for API requests');
