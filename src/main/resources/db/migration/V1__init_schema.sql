-- ============================================================
-- V1 - TripJoy consolidated schema
-- Fresh-install baseline after migration reset/refactor.
-- Contains schema only. Runtime/demo data is intentionally not seeded here.
-- ============================================================

-- ============================================================
-- Extensions
-- ============================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;

-- Accent-insensitive search helper. Kept immutable so expression indexes can use it.
CREATE OR REPLACE FUNCTION f_unaccent(text)
    RETURNS text
    LANGUAGE plpgsql IMMUTABLE PARALLEL SAFE STRICT
AS $$
BEGIN
    RETURN public.unaccent($1);
END;
$$;

-- ============================================================
-- Auth and RBAC
-- ============================================================
CREATE TABLE invalidated_token (
    id         UUID PRIMARY KEY,
    expires_at TIMESTAMP
);

CREATE TABLE permission (
    name        VARCHAR(255) PRIMARY KEY,
    description VARCHAR(255)
);

CREATE TABLE role (
    name        VARCHAR(255) PRIMARY KEY,
    description VARCHAR(255)
);

CREATE TABLE role_permission (
    role_name       VARCHAR(255) NOT NULL REFERENCES role(name) ON DELETE CASCADE,
    permission_name VARCHAR(255) NOT NULL REFERENCES permission(name) ON DELETE CASCADE,
    PRIMARY KEY (role_name, permission_name)
);

CREATE TABLE users (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at        TIMESTAMP,
    created_by        UUID,
    updated_at        TIMESTAMP,
    updated_by        UUID,
    username          VARCHAR(255),
    password          VARCHAR(255),
    email             VARCHAR(255),
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_number      VARCHAR(255),
    full_name         VARCHAR(255),
    bio               TEXT,
    avatar_url        VARCHAR(255),
    date_of_birth     DATE,
    credits           BIGINT,
    is_locked         BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted        BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMP,
    deleted_by        VARCHAR(255)
);

CREATE TABLE user_role (
    users_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_name VARCHAR(255) NOT NULL REFERENCES role(name) ON DELETE CASCADE,
    PRIMARY KEY (users_id, role_name)
);

-- ============================================================
-- Core travel domain
-- ============================================================
CREATE TABLE theme (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP,
    created_by UUID,
    updated_at TIMESTAMP,
    updated_by UUID,
    name       VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE location (
    id                         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at                 TIMESTAMP,
    created_by                 UUID,
    updated_at                 TIMESTAMP,
    updated_by                 UUID,
    location_type              VARCHAR(30) NOT NULL DEFAULT 'POI',
    is_verified                BOOLEAN NOT NULL DEFAULT FALSE,
    usage_count                INTEGER NOT NULL DEFAULT 0,
    provider                   VARCHAR(20),
    provider_id                VARCHAR(255) UNIQUE,
    name                       VARCHAR(500) NOT NULL,
    name_en                    VARCHAR(500),
    full_address               TEXT,
    place_formatted            TEXT,
    coordinates                GEOMETRY(Point, 4326),
    latitude                   DOUBLE PRECISION NOT NULL,
    longitude                  DOUBLE PRECISION NOT NULL,
    routable_latitude          DOUBLE PRECISION,
    routable_longitude         DOUBLE PRECISION,
    viewport                   JSONB,
    country_name               VARCHAR(100),
    country_code               VARCHAR(3),
    admin_area_level1          VARCHAR(150),
    admin_area_level1_code     VARCHAR(20),
    admin_area_level2          VARCHAR(150),
    admin_area_level3          VARCHAR(150),
    city                       VARCHAR(150),
    sub_locality               VARCHAR(150),
    neighborhood               VARCHAR(150),
    street_name                VARCHAR(200),
    address_number             VARCHAR(30),
    postcode                   VARCHAR(20),
    plus_code                  VARCHAR(20),
    poi_categories             TEXT,
    primary_type               VARCHAR(100),
    maki                       VARCHAR(50),
    icon_url                   VARCHAR(500),
    icon_background_color      VARCHAR(10),
    rating                     NUMERIC(3,1),
    user_ratings_total         INTEGER,
    price_level                INTEGER,
    operational_status         VARCHAR(30),
    hotline                    VARCHAR(50),
    website                    VARCHAR(1000),
    opening_hours              JSONB,
    admin_code                 VARCHAR(20),
    timezone                   VARCHAR(50),
    wheelchair_accessible      BOOLEAN,
    search_vector              TSVECTOR,
    raw_response               JSONB,
    is_deleted                 BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at                 TIMESTAMP,
    deleted_by                 VARCHAR(255)
);

CREATE INDEX idx_location_postgis_geom ON location USING GIST (coordinates);
CREATE INDEX idx_location_provider_id ON location(provider_id);
CREATE INDEX idx_location_type ON location(location_type);
CREATE INDEX idx_location_type_verified ON location(location_type, is_verified);
CREATE INDEX idx_location_country_type ON location(country_code, location_type);
CREATE INDEX idx_location_usage_count ON location(usage_count DESC);
CREATE INDEX idx_location_coordinates ON location(latitude, longitude);
CREATE INDEX idx_location_search_vector ON location USING GIN (search_vector);

CREATE OR REPLACE FUNCTION location_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('simple', f_unaccent(coalesce(NEW.name, ''))), 'A') ||
        setweight(to_tsvector('simple', f_unaccent(coalesce(NEW.name_en, ''))), 'B') ||
        setweight(to_tsvector('simple', f_unaccent(coalesce(NEW.full_address, ''))), 'B') ||
        setweight(to_tsvector('simple', f_unaccent(coalesce(NEW.poi_categories, ''))), 'D');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_location_search_vector_update
    BEFORE INSERT OR UPDATE ON location
    FOR EACH ROW EXECUTE FUNCTION location_search_vector_update();

CREATE TABLE groups (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at    TIMESTAMP,
    created_by    UUID,
    updated_at    TIMESTAMP,
    updated_by    UUID,
    name          VARCHAR(255),
    description   TEXT,
    chatbot_count INTEGER DEFAULT 0,
    avatar        VARCHAR(500),
    theme_color   VARCHAR(20),
    is_pro        BOOLEAN DEFAULT FALSE,
    is_deleted    BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP,
    deleted_by    VARCHAR(255)
);

CREATE INDEX idx_group_name_unaccent ON groups (lower(f_unaccent(name)));

CREATE TABLE group_member (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP,
    created_by UUID,
    updated_at TIMESTAMP,
    updated_by UUID,
    group_id   UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    role       VARCHAR(50),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX idx_group_member_lookup ON group_member(group_id, user_id);
CREATE INDEX idx_user_groups ON group_member(user_id, is_deleted);

CREATE TABLE itinerary (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMP,
    created_by      UUID,
    updated_at      TIMESTAMP,
    updated_by      UUID,
    name            VARCHAR(255),
    description     TEXT,
    start_date      TIMESTAMP,
    end_date        TIMESTAMP,
    people_quantity INTEGER,
    budget_estimate NUMERIC(19,2),
    status          VARCHAR(50),
    group_id        UUID REFERENCES groups(id),
    user_id         UUID REFERENCES users(id),
    origin          UUID REFERENCES location(id),
    destination     UUID REFERENCES location(id),
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(255)
);

CREATE INDEX idx_itinerary_group ON itinerary(group_id, is_deleted);
CREATE INDEX idx_itinerary_user_deleted ON itinerary(user_id, is_deleted);

CREATE TABLE itinerary_theme_mapping (
    itinerary_id UUID NOT NULL REFERENCES itinerary(id) ON DELETE CASCADE,
    theme_id     UUID NOT NULL REFERENCES theme(id) ON DELETE CASCADE,
    PRIMARY KEY (itinerary_id, theme_id)
);

CREATE TABLE favourite_itinerary (
    itinerary_id UUID NOT NULL REFERENCES itinerary(id) ON DELETE CASCADE,
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (itinerary_id, user_id)
);

CREATE TABLE trip_item (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at   TIMESTAMP,
    created_by   UUID,
    updated_at   TIMESTAMP,
    updated_by   UUID,
    start_time   TIMESTAMP,
    duration     INTEGER,
    note         TEXT,
    location_id  UUID REFERENCES location(id),
    itinerary_id UUID REFERENCES itinerary(id) ON DELETE CASCADE
);

CREATE INDEX idx_trip_item_itinerary_id ON trip_item(itinerary_id);

CREATE TABLE travel_notebook (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at         TIMESTAMP,
    created_by         UUID,
    updated_at         TIMESTAMP,
    updated_by         UUID,
    name               VARCHAR(255),
    food               TEXT,
    climate            TEXT,
    culture            TEXT,
    emergency_contacts TEXT,
    itinerary_id       UUID UNIQUE REFERENCES itinerary(id) ON DELETE CASCADE
);

CREATE TABLE expense (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at   TIMESTAMP,
    created_by   UUID,
    updated_at   TIMESTAMP,
    updated_by   UUID,
    name         VARCHAR(255),
    description  TEXT,
    type         VARCHAR(255),
    method       VARCHAR(255),
    amount       NUMERIC(19,2),
    itinerary_id UUID NOT NULL REFERENCES itinerary(id) ON DELETE CASCADE,
    user_id      UUID NOT NULL REFERENCES users(id)
);

CREATE INDEX idx_expense_itinerary_id ON expense(itinerary_id);

CREATE TABLE suggest_location (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP,
    created_by  UUID,
    updated_at  TIMESTAMP,
    updated_by  UUID,
    user_id     UUID NOT NULL REFERENCES users(id),
    group_id    UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    location_id UUID NOT NULL REFERENCES location(id),
    notes       TEXT
);

CREATE INDEX idx_suggest_location ON suggest_location(location_id);

-- ============================================================
-- Social content
-- ============================================================
CREATE TABLE post (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at     TIMESTAMP,
    created_by     UUID,
    updated_at     TIMESTAMP,
    updated_by     UUID,
    content        TEXT,
    share_quantity INTEGER DEFAULT 0,
    visibility     VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    itinerary_id   UUID REFERENCES itinerary(id),
    creator_id     UUID REFERENCES users(id),
    is_deleted     BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at     TIMESTAMP,
    deleted_by     VARCHAR(255)
);

CREATE INDEX idx_post_content_unaccent
    ON post USING GIN (to_tsvector('simple', f_unaccent(coalesce(content, ''))));

CREATE TABLE post_media (
    post_id     UUID NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    media_url   VARCHAR(1024),
    media_order INTEGER
);

CREATE TABLE hashtag (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP,
    created_by UUID,
    updated_at TIMESTAMP,
    updated_by UUID,
    name       VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE post_hashtag_mapping (
    post_id    UUID NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    hashtag_id UUID NOT NULL REFERENCES hashtag(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, hashtag_id)
);

CREATE TABLE like_post (
    post_id UUID NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, user_id)
);

CREATE TABLE save_post (
    post_id UUID NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, user_id)
);

CREATE TABLE comment (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at        TIMESTAMP,
    created_by        UUID,
    updated_at        TIMESTAMP,
    updated_by        UUID,
    content           TEXT,
    is_deleted        BOOLEAN DEFAULT FALSE,
    parent_comment_id UUID REFERENCES comment(id),
    user_id           UUID NOT NULL REFERENCES users(id),
    post_id           UUID NOT NULL REFERENCES post(id) ON DELETE CASCADE
);

CREATE TABLE like_comment (
    comment_id UUID NOT NULL REFERENCES comment(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (comment_id, user_id)
);

-- ============================================================
-- Messaging
-- ============================================================
CREATE TABLE conversation (
    id                         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at                 TIMESTAMP,
    created_by                 UUID,
    updated_at                 TIMESTAMP,
    updated_by                 UUID,
    type                       VARCHAR(50),
    group_id                   UUID REFERENCES groups(id),
    name                       VARCHAR(255),
    last_message_timestamp     TIMESTAMP,
    last_message_id            UUID,
    last_message_content       TEXT,
    last_message_type          VARCHAR(50),
    last_message_sender_id     UUID,
    last_message_sender_name   VARCHAR(255),
    last_message_sender_avatar VARCHAR(500)
);

CREATE INDEX idx_conversation_group ON conversation(group_id);
CREATE INDEX idx_conversation_timestamp ON conversation(last_message_timestamp DESC);

CREATE TABLE conversation_member (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at           TIMESTAMP,
    created_by           UUID,
    updated_at           TIMESTAMP,
    updated_by           UUID,
    conversation_id      UUID REFERENCES conversation(id) ON DELETE CASCADE,
    user_id              UUID REFERENCES users(id) ON DELETE CASCADE,
    unread_count         BIGINT DEFAULT 0,
    is_muted             BOOLEAN DEFAULT FALSE,
    is_pinned            BOOLEAN NOT NULL DEFAULT FALSE,
    last_read_message_id UUID
);

CREATE INDEX idx_conversation_member_lookup ON conversation_member(conversation_id, user_id);
CREATE INDEX idx_user_conversations ON conversation_member(user_id);

CREATE TABLE chat_message (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at        TIMESTAMP,
    created_by        UUID,
    updated_at        TIMESTAMP,
    updated_by        UUID,
    message_type      VARCHAR(255),
    message_content   TEXT,
    media_url         VARCHAR(255),
    shared_post_id    UUID REFERENCES post(id) ON DELETE SET NULL,
    is_bot            BOOLEAN DEFAULT FALSE,
    status            VARCHAR(50),
    is_pinned         BOOLEAN NOT NULL DEFAULT FALSE,
    parent_message_id UUID REFERENCES chat_message(id),
    sender_id         UUID NOT NULL REFERENCES users(id),
    conversation_id   UUID NOT NULL REFERENCES conversation(id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_message_cursor ON chat_message(conversation_id, created_at DESC);
CREATE INDEX idx_chat_message_content_unaccent
    ON chat_message USING GIN (to_tsvector('simple', f_unaccent(coalesce(message_content, ''))));

CREATE TABLE like_chat_message (
    chat_message_id UUID NOT NULL REFERENCES chat_message(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (chat_message_id, user_id)
);

-- ============================================================
-- Notifications, audit, and admin config
-- ============================================================
CREATE TABLE notifications (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP,
    created_by  UUID,
    updated_at  TIMESTAMP,
    updated_by  UUID,
    recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id    UUID REFERENCES users(id),
    type        VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id   VARCHAR(255),
    title       VARCHAR(255),
    message     TEXT,
    metadata    TEXT,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    read_at     TIMESTAMP,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    priority    VARCHAR(20)
);

CREATE INDEX idx_recipient_unread ON notifications(recipient_id, is_read, created_at);
CREATE INDEX idx_recipient_created ON notifications(recipient_id, created_at);
CREATE INDEX idx_entity ON notifications(entity_type, entity_id);
CREATE INDEX idx_actor ON notifications(actor_id, created_at);

CREATE TABLE activity_logs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP,
    created_by  UUID,
    updated_at  TIMESTAMP,
    updated_by  UUID,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action      VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id   VARCHAR(255),
    metadata    TEXT,
    ip_address  VARCHAR(45),
    user_agent  TEXT
);

CREATE INDEX idx_user_action ON activity_logs(user_id, action, created_at);
CREATE INDEX idx_activity_entity ON activity_logs(entity_type, entity_id);
CREATE INDEX idx_created_at ON activity_logs(created_at);

CREATE TABLE system_config (
    config_key   VARCHAR(100) PRIMARY KEY,
    config_value TEXT NOT NULL,
    data_type    VARCHAR(20) NOT NULL,
    config_group VARCHAR(50) NOT NULL,
    description  TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(100)
);

-- ============================================================
-- Reports, feedback, and moderation
-- ============================================================
CREATE TABLE report_content (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at       TIMESTAMP,
    created_by       UUID,
    updated_at       TIMESTAMP,
    updated_by       UUID,
    content_type     VARCHAR(30) NOT NULL,
    target_id        UUID NOT NULL,
    report_type      VARCHAR(80) NOT NULL,
    description      TEXT,
    text             TEXT,
    media_url        VARCHAR(1024),
    status           VARCHAR(30) NOT NULL,
    reporter_id      UUID NOT NULL REFERENCES users(id),
    reported_user_id UUID NOT NULL REFERENCES users(id)
);

CREATE INDEX idx_report_content_status_created ON report_content(status, created_at DESC);
CREATE INDEX idx_report_content_target ON report_content(content_type, target_id);
CREATE INDEX idx_report_content_reporter ON report_content(reporter_id, created_at DESC);
CREATE INDEX idx_report_content_reported_user ON report_content(reported_user_id, created_at DESC);

CREATE TABLE moderation_action (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at        TIMESTAMP,
    created_by        UUID,
    updated_at        TIMESTAMP,
    updated_by        UUID,
    action_type       VARCHAR(80),
    note              TEXT,
    user_id           UUID NOT NULL REFERENCES users(id),
    ba_id             UUID NOT NULL REFERENCES users(id),
    report_content_id UUID REFERENCES report_content(id)
);

CREATE INDEX idx_moderation_action_user_created ON moderation_action(user_id, created_at DESC);

CREATE TABLE handle_report_content (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at           TIMESTAMP,
    created_by           UUID,
    updated_at           TIMESTAMP,
    updated_by           UUID,
    report_type          VARCHAR(80),
    status               VARCHAR(30),
    description          TEXT,
    report_content_id    UUID NOT NULL REFERENCES report_content(id),
    ba_id                UUID NOT NULL REFERENCES users(id),
    moderation_action_id UUID REFERENCES moderation_action(id)
);

CREATE INDEX idx_handle_report_content_report ON handle_report_content(report_content_id, created_at DESC);

CREATE TABLE feedback (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at         TIMESTAMP,
    created_by         UUID,
    updated_at         TIMESTAMP,
    updated_by         UUID,
    type               VARCHAR(80),
    title              VARCHAR(255),
    content            TEXT,
    status             VARCHAR(30),
    sender_id          UUID NOT NULL REFERENCES users(id),
    receiver_id        UUID REFERENCES users(id),
    parent_feedback_id UUID REFERENCES feedback(id),
    report_content_id  UUID REFERENCES report_content(id)
);

CREATE INDEX idx_feedback_status_created ON feedback(status, created_at DESC);
CREATE INDEX idx_feedback_sender_created ON feedback(sender_id, created_at DESC);
CREATE INDEX idx_feedback_report ON feedback(report_content_id, created_at DESC);
