-- ============================================================
-- V9 - Seed initial system_config data
-- Covers all config keys referenced across the codebase.
-- ============================================================

INSERT INTO system_config (config_key, config_value, data_type, config_group, description, created_at, updated_at, updated_by)
VALUES

-- ============================================================
-- GROUP: AI — AI service tuning & behaviour
-- ============================================================
('AI_TIMEOUT_SECONDS',        '120',    'INT',     'AI',      'Timeout (seconds) for AI HTTP requests',                   NOW(), NOW(), 'SYSTEM'),
('AI_CB_SLIDING_WINDOW_SIZE', '5',      'INT',     'AI',      'Circuit Breaker: number of calls in sliding window',       NOW(), NOW(), 'SYSTEM'),
('AI_CB_MIN_CALLS',           '3',      'INT',     'AI',      'Circuit Breaker: minimum calls before evaluating rate',    NOW(), NOW(), 'SYSTEM'),
('AI_CB_FAILURE_RATE_THRESHOLD', '50',  'INT',     'AI',      'Circuit Breaker: failure rate threshold (%) to open',      NOW(), NOW(), 'SYSTEM'),
('AI_RETRY_MAX_ATTEMPTS',     '3',      'INT',     'AI',      'Retry: maximum number of attempts for AI calls',           NOW(), NOW(), 'SYSTEM'),
('AI_RETRY_WAIT_DURATION',    '2',      'INT',     'AI',      'Retry: wait duration (seconds) between attempts',          NOW(), NOW(), 'SYSTEM'),

-- ============================================================
-- GROUP: CHAT — Chat & messaging limits
-- ============================================================
('CHAT_MSG_RATE_LIMIT',       '30',     'INT',     'CHAT',    'Max chat messages per user per minute (Socket.IO)',        NOW(), NOW(), 'SYSTEM'),
('CHAT_TYPING_RATE_LIMIT',    '60',     'INT',     'CHAT',    'Max typing events per user per minute (Socket.IO)',        NOW(), NOW(), 'SYSTEM'),
('CHAT_MAX_PINNED_MESSAGES',  '50',     'INT',     'CHAT',    'Max number of pinned messages allowed per conversation',   NOW(), NOW(), 'SYSTEM'),

-- ============================================================
-- GROUP: SYSTEM — General system-wide limits
-- ============================================================
('SYSTEM_MAX_PAGE_SIZE',           '200',  'INT',  'SYSTEM',  'Maximum page size allowed for paginated queries',          NOW(), NOW(), 'SYSTEM'),
('SYSTEM_DEFAULT_SEARCH_RADIUS',   '5000', 'INT',  'SYSTEM',  'Default location search radius in meters',                 NOW(), NOW(), 'SYSTEM'),
('SYSTEM_MAX_SEARCH_RADIUS',       '50000','INT',  'SYSTEM',  'Maximum location search radius in meters',                 NOW(), NOW(), 'SYSTEM')

ON CONFLICT (config_key) DO NOTHING;
