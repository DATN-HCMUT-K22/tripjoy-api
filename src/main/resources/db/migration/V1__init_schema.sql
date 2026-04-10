-- ============================================================
-- V1 — TripJoy Initial Schema
-- Generated from Java Entity definitions
-- Replaces Hibernate ddl-auto: update
-- ============================================================

-- ============================================================
-- EXTENSIONS (required for GEOMETRY, pg_trgm, unaccent)
-- These must run before any table that uses geometry types.
-- On local Docker: also created by init-postgis.sh
-- On AWS RDS: only created here (init-postgis.sh doesn't run on RDS)
-- ============================================================
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;

-- ============================================================
-- AUTH & SECURITY
-- ============================================================

CREATE TABLE IF NOT EXISTS invalidated_token (
    id          UUID        PRIMARY KEY,
    expires_at  TIMESTAMP
);

-- ============================================================
-- RBAC: permission, role, role_permission
-- ============================================================

CREATE TABLE IF NOT EXISTS permission (
    name        VARCHAR(255) PRIMARY KEY,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS role (
    name        VARCHAR(255) PRIMARY KEY,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS role_permission (
    role_name       VARCHAR(255) NOT NULL REFERENCES role(name) ON DELETE CASCADE,
    permission_name VARCHAR(255) NOT NULL REFERENCES permission(name) ON DELETE CASCADE,
    PRIMARY KEY (role_name, permission_name)
);

-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at          TIMESTAMP,
    created_by          UUID,
    updated_at          TIMESTAMP,
    updated_by          UUID,
    username            VARCHAR(255),
    password            VARCHAR(255),
    email               VARCHAR(255),
    is_email_verified   BOOLEAN         NOT NULL DEFAULT FALSE,
    phone_number        VARCHAR(255),
    full_name           VARCHAR(255),
    bio                 TEXT,
    avatar_url          VARCHAR(255),
    date_of_birth       DATE,
    credits             BIGINT,
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    deleted_by          VARCHAR(255),
    is_locked           BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS user_role (
    users_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_name   VARCHAR(255) NOT NULL REFERENCES role(name) ON DELETE CASCADE,
    PRIMARY KEY (users_id, role_name)
);

-- ============================================================
-- THEME (travel themes: Beach, Mountain, Culture, ...)
-- ============================================================

CREATE TABLE IF NOT EXISTS theme (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP,
    created_by  UUID,
    updated_at  TIMESTAMP,
    updated_by  UUID,
    name        VARCHAR(255) NOT NULL UNIQUE
);

-- ============================================================
-- LOCATION (Two-Tier: Administrative + POI)
-- ============================================================

CREATE TABLE IF NOT EXISTS location (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at                  TIMESTAMP,
    created_by                  UUID,
    updated_at                  TIMESTAMP,
    updated_by                  UUID,

    -- Core identity
    name                        VARCHAR(500)    NOT NULL,
    name_en                     VARCHAR(500),
    full_address                TEXT,
    place_formatted             TEXT,

    -- Coordinates
    latitude                    DOUBLE PRECISION,
    longitude                   DOUBLE PRECISION,
    routable_latitude           DOUBLE PRECISION,
    routable_longitude          DOUBLE PRECISION,
    coordinates                 GEOMETRY(Point, 4326),

    -- Viewport for map fit
    viewport                    TEXT,

    -- Classification
    location_type               VARCHAR(30)     NOT NULL DEFAULT 'POI',
    is_verified                 BOOLEAN         NOT NULL DEFAULT FALSE,
    usage_count                 INTEGER         NOT NULL DEFAULT 0,

    -- Provider (map source)
    provider                    VARCHAR(50),
    provider_id                 VARCHAR(500)    UNIQUE,

    -- Address components (embedded AddressComponents)
    country_name                VARCHAR(100),
    country_code                VARCHAR(3),
    admin_area_level1           VARCHAR(150),
    admin_area_level1_code      VARCHAR(20),
    admin_area_level2           VARCHAR(150),
    admin_area_level3           VARCHAR(150),
    city                        VARCHAR(150),
    sub_locality                VARCHAR(150),
    neighborhood                VARCHAR(150),
    street_name                 VARCHAR(200),
    address_number              VARCHAR(30),
    postcode                    VARCHAR(20),
    plus_code                   VARCHAR(20),

    -- Administrative shortcode (for seeded tier-1 data)
    admin_code                  VARCHAR(20),
    timezone                    VARCHAR(50),

    -- POI-specific metadata
    poi_categories              TEXT,
    primary_type                VARCHAR(100),
    maki                        VARCHAR(50),
    icon_url                    VARCHAR(500),
    icon_background_color       VARCHAR(10),
    rating                      NUMERIC(3,1),
    user_ratings_total          INTEGER,
    price_level                 INTEGER,
    operational_status          VARCHAR(30),
    hotline                     VARCHAR(50),
    website                     VARCHAR(1000),
    opening_hours               TEXT,
    wheelchair_accessible       BOOLEAN,
    raw_response                TEXT,

    -- Full-text search
    search_vector               TSVECTOR,

    -- Soft delete
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at                  TIMESTAMP,
    deleted_by                  VARCHAR(255)
);

-- ============================================================
-- GROUPS
-- ============================================================

CREATE TABLE IF NOT EXISTS groups (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMP,
    created_by      UUID,
    updated_at      TIMESTAMP,
    updated_by      UUID,
    name            VARCHAR(255),
    description     TEXT,
    chatbot_count   INTEGER         DEFAULT 0,
    avatar          VARCHAR(500),
    theme_color     VARCHAR(20),
    is_pro          BOOLEAN         DEFAULT FALSE,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS group_member (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMP,
    created_by      UUID,
    updated_at      TIMESTAMP,
    updated_by      UUID,
    group_id        UUID        NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id         UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role            VARCHAR(50),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_group_member_lookup ON group_member(group_id, user_id);
CREATE INDEX IF NOT EXISTS idx_user_groups ON group_member(user_id, is_deleted);

-- ============================================================
-- ITINERARY & TRIP ITEMS
-- ============================================================

CREATE TABLE IF NOT EXISTS itinerary (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at          TIMESTAMP,
    created_by          UUID,
    updated_at          TIMESTAMP,
    updated_by          UUID,
    name                VARCHAR(255),
    description         TEXT,
    start_date          TIMESTAMP,
    end_date            TIMESTAMP,
    people_quantity     INTEGER,
    budget_estimate     NUMERIC(19,2),
    status              VARCHAR(50),
    group_id            UUID            REFERENCES groups(id),
    user_id             UUID            REFERENCES users(id),
    origin              UUID            REFERENCES location(id),
    destination         UUID            REFERENCES location(id),
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    deleted_by          VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_itinerary_group ON itinerary(group_id, is_deleted);

CREATE TABLE IF NOT EXISTS itinerary_theme_mapping (
    itinerary_id    UUID    NOT NULL REFERENCES itinerary(id) ON DELETE CASCADE,
    theme_id        UUID    NOT NULL REFERENCES theme(id) ON DELETE CASCADE,
    PRIMARY KEY (itinerary_id, theme_id)
);

CREATE TABLE IF NOT EXISTS favourite_itinerary (
    itinerary_id    UUID    NOT NULL REFERENCES itinerary(id) ON DELETE CASCADE,
    user_id         UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (itinerary_id, user_id)
);

CREATE TABLE IF NOT EXISTS trip_item (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMP,
    created_by      UUID,
    updated_at      TIMESTAMP,
    updated_by      UUID,
    start_time      TIMESTAMP,
    duration        INTEGER,
    note            TEXT,
    location_id     UUID        REFERENCES location(id),
    itinerary_id    UUID        REFERENCES itinerary(id) ON DELETE CASCADE
);

-- ============================================================
-- TRAVEL NOTEBOOK (attached to an Itinerary)
-- ============================================================

CREATE TABLE IF NOT EXISTS travel_notebook (
    id                  UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at          TIMESTAMP,
    created_by          UUID,
    updated_at          TIMESTAMP,
    updated_by          UUID,
    name                VARCHAR(255),
    description         TEXT,
    weather_forecast    TEXT,
    culture_etiquette   TEXT,
    emergency_contacts  TEXT,
    packing_guide       TEXT,
    itinerary_id        UUID    UNIQUE REFERENCES itinerary(id) ON DELETE CASCADE
);

-- ============================================================
-- EXPENSE (trip budget tracking)
-- ============================================================

CREATE TABLE IF NOT EXISTS expense (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMP,
    created_by      UUID,
    updated_at      TIMESTAMP,
    updated_by      UUID,
    name            VARCHAR(255),
    description     TEXT,
    type            VARCHAR(100),
    method          VARCHAR(100),
    amount          NUMERIC(19,2),
    itinerary_id    UUID            NOT NULL REFERENCES itinerary(id) ON DELETE CASCADE,
    user_id         UUID            NOT NULL REFERENCES users(id)
);

-- ============================================================
-- SUGGEST LOCATION (group voting on destination)
-- ============================================================

CREATE TABLE IF NOT EXISTS suggest_location (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMP,
    created_by      UUID,
    updated_at      TIMESTAMP,
    updated_by      UUID,
    user_id         UUID    NOT NULL REFERENCES users(id),
    group_id        UUID    NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    location_id     UUID    NOT NULL REFERENCES location(id),
    notes           TEXT
);

CREATE INDEX IF NOT EXISTS idx_suggest_location ON suggest_location(location_id);

-- ============================================================
-- POSTS & SOCIAL
-- ============================================================

CREATE TABLE IF NOT EXISTS post (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMP,
    created_by      UUID,
    updated_at      TIMESTAMP,
    updated_by      UUID,
    content         TEXT,
    share_quantity  INTEGER DEFAULT 0,
    itinerary_id    UUID    REFERENCES itinerary(id),
    creator_id      UUID    REFERENCES users(id),
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS post_media (
    post_id         UUID        NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    media_url       VARCHAR(1024),
    media_order     INTEGER
);

CREATE TABLE IF NOT EXISTS hashtag (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP,
    created_by  UUID,
    updated_at  TIMESTAMP,
    updated_by  UUID,
    name        VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS post_hashtag_mapping (
    post_id     UUID    NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    hashtag_id  UUID    NOT NULL REFERENCES hashtag(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, hashtag_id)
);

CREATE TABLE IF NOT EXISTS like_post (
    post_id     UUID    NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    user_id     UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, user_id)
);

CREATE TABLE IF NOT EXISTS save_post (
    post_id     UUID    NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    user_id     UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, user_id)
);

CREATE TABLE IF NOT EXISTS comment (
    id                  UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at          TIMESTAMP,
    created_by          UUID,
    updated_at          TIMESTAMP,
    updated_by          UUID,
    content             TEXT,
    is_deleted          BOOLEAN DEFAULT FALSE,
    parent_comment_id   UUID    REFERENCES comment(id),
    user_id             UUID    NOT NULL REFERENCES users(id),
    post_id             UUID    NOT NULL REFERENCES post(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS like_comment (
    comment_id  UUID    NOT NULL REFERENCES comment(id) ON DELETE CASCADE,
    user_id     UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (comment_id, user_id)
);

-- ============================================================
-- MESSAGING: conversation, conversation_member, chat_message
-- ============================================================

CREATE TABLE IF NOT EXISTS conversation (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at              TIMESTAMP,
    created_by              UUID,
    updated_at              TIMESTAMP,
    updated_by              UUID,
    type                    VARCHAR(50),
    group_id                UUID        REFERENCES groups(id),
    name                    VARCHAR(255),
    last_message_timestamp  TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_conversation_group     ON conversation(group_id);
CREATE INDEX IF NOT EXISTS idx_conversation_timestamp ON conversation(last_message_timestamp DESC);

CREATE TABLE IF NOT EXISTS conversation_member (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at          TIMESTAMP,
    created_by          UUID,
    updated_at          TIMESTAMP,
    updated_by          UUID,
    conversation_id     UUID        REFERENCES conversation(id) ON DELETE CASCADE,
    user_id             UUID        REFERENCES users(id) ON DELETE CASCADE,
    unread_count        BIGINT      DEFAULT 0,
    is_muted            BOOLEAN     DEFAULT FALSE,
    is_pinned           BOOLEAN     NOT NULL DEFAULT FALSE,
    last_read_message_id UUID
);

CREATE INDEX IF NOT EXISTS idx_conversation_member_lookup ON conversation_member(conversation_id, user_id);
CREATE INDEX IF NOT EXISTS idx_user_conversations         ON conversation_member(user_id);

CREATE TABLE IF NOT EXISTS chat_message (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at          TIMESTAMP,
    created_by          UUID,
    updated_at          TIMESTAMP,
    updated_by          UUID,
    message_type        VARCHAR(50),
    message_content     TEXT,
    media_url           VARCHAR(500),
    shared_post_url     VARCHAR(500),
    is_bot              BOOLEAN     DEFAULT FALSE,
    status              VARCHAR(50),
    is_pinned           BOOLEAN     NOT NULL DEFAULT FALSE,
    parent_message_id   UUID        REFERENCES chat_message(id),
    sender_id           UUID        NOT NULL REFERENCES users(id),
    conversation_id     UUID        NOT NULL REFERENCES conversation(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chat_message_cursor ON chat_message(conversation_id, created_at DESC);

CREATE TABLE IF NOT EXISTS like_chat_message (
    chat_message_id UUID    NOT NULL REFERENCES chat_message(id) ON DELETE CASCADE,
    user_id         UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (chat_message_id, user_id)
);

-- ============================================================
-- NOTIFICATIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS notifications (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMP,
    created_by      UUID,
    updated_at      TIMESTAMP,
    updated_by      UUID,
    recipient_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id        UUID        REFERENCES users(id),
    type            VARCHAR(50) NOT NULL,
    entity_type     VARCHAR(50),
    entity_id       VARCHAR(255),
    title           VARCHAR(255),
    message         TEXT,
    metadata        TEXT,
    is_read         BOOLEAN     NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMP,
    is_archived     BOOLEAN     NOT NULL DEFAULT FALSE,
    priority        VARCHAR(20)
);

CREATE INDEX IF NOT EXISTS idx_recipient_unread   ON notifications(recipient_id, is_read, created_at);
CREATE INDEX IF NOT EXISTS idx_recipient_created  ON notifications(recipient_id, created_at);
CREATE INDEX IF NOT EXISTS idx_entity             ON notifications(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_actor              ON notifications(actor_id, created_at);

-- ============================================================
-- ACTIVITY LOGS (audit trail)
-- ============================================================

CREATE TABLE IF NOT EXISTS activity_logs (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP,
    created_by  UUID,
    updated_at  TIMESTAMP,
    updated_by  UUID,

    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action      VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id   VARCHAR(255),
    metadata    TEXT,
    ip_address  VARCHAR(45),
    user_agent  TEXT
);

CREATE INDEX IF NOT EXISTS idx_user_action       ON activity_logs(user_id, action, created_at);
CREATE INDEX IF NOT EXISTS idx_activity_entity   ON activity_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_created_at        ON activity_logs(created_at);
