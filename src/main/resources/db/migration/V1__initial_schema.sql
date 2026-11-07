-- ============================================================
-- Henri Trip - Initial database schema
-- PostgreSQL 18+
--
-- Schema ownership: Flyway
-- Business-level aggregate validation: Spring services
-- Database-level integrity: PostgreSQL constraints
-- ============================================================

-- ============================================================
-- 1. ENUM TYPES
-- ============================================================

CREATE TYPE user_role AS ENUM (
    'USER',
    'ADMIN'
);

CREATE TYPE transport_option AS ENUM (
    'CAR',
    'BICYCLE',
    'WALKING',
    'MOTORCYCLE'
);

CREATE TYPE season_option AS ENUM (
    'SUMMER',
    'SPRING',
    'AUTUMN',
    'WINTER'
);

CREATE TYPE audience_option AS ENUM (
    'FAMILY',
    'SOLO',
    'GROUP',
    'FRIENDS'
);

CREATE TYPE activity_category AS ENUM (
    'MUSEUM',
    'CASTLE',
    'ACTIVITY',
    'PARK',
    'CAVE'
);


-- ============================================================
-- 2. USERS
-- ============================================================

CREATE TABLE users (
    id 				BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email           VARCHAR(320) NOT NULL,
    password_hash   TEXT NOT NULL,
    role            user_role NOT NULL DEFAULT 'USER',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_users_email_not_empty
        CHECK (LENGTH(TRIM(email)) > 0),

    CONSTRAINT ck_users_password_hash_not_empty
        CHECK (LENGTH(password_hash) > 0)
);

-- Case-insensitive email uniqueness.
-- Application must also normalize email before persistence.
CREATE UNIQUE INDEX uq_users_email_lower
    ON users (LOWER(BTRIM(email)));


-- ============================================================
-- 3. GUIDES
-- ============================================================

CREATE TABLE guides (
    id 				BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title               VARCHAR(200) NOT NULL,
    description         TEXT NOT NULL,
    created_by_user_id  BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_guides_title_not_empty
        CHECK (LENGTH(TRIM(title)) > 0),

    CONSTRAINT ck_guides_description_not_empty
        CHECK (LENGTH(TRIM(description)) > 0),

    CONSTRAINT fk_guides_creator
        FOREIGN KEY (created_by_user_id)
        REFERENCES users (id)
        ON DELETE SET NULL
);

CREATE INDEX idx_guides_created_by
    ON guides (created_by_user_id);


-- ============================================================
-- 4. GUIDE OPTIONS
-- ============================================================

CREATE TABLE guide_transports (
    guide_id BIGINT NOT NULL,
    transport transport_option NOT NULL,

    PRIMARY KEY (guide_id, transport),

    CONSTRAINT fk_guide_transports_guide
        FOREIGN KEY (guide_id)
        REFERENCES guides (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_guide_transports_filter
    ON guide_transports (transport, guide_id);


CREATE TABLE guide_seasons (
    guide_id BIGINT NOT NULL,
    season season_option NOT NULL,

    PRIMARY KEY (guide_id, season),

    CONSTRAINT fk_guide_seasons_guide
        FOREIGN KEY (guide_id)
        REFERENCES guides (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_guide_seasons_filter
    ON guide_seasons (season, guide_id);


CREATE TABLE guide_audiences (
    guide_id BIGINT NOT NULL,
    audience audience_option NOT NULL,

    PRIMARY KEY (guide_id, audience),

    CONSTRAINT fk_guide_audiences_guide
        FOREIGN KEY (guide_id)
        REFERENCES guides (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_guide_audiences_filter
    ON guide_audiences (audience, guide_id);


-- ============================================================
-- 5. GUIDE DAYS
-- ============================================================

CREATE TABLE guide_days (
    id 			BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    guide_id    BIGINT NOT NULL,
    day_number  INTEGER NOT NULL,

    CONSTRAINT fk_guide_days_guide
        FOREIGN KEY (guide_id)
        REFERENCES guides (id)
        ON DELETE CASCADE,

    CONSTRAINT ck_guide_days_number
        CHECK (day_number >= 1),

    CONSTRAINT uq_guide_days_number
        UNIQUE (guide_id, day_number)
        DEFERRABLE INITIALLY DEFERRED
);


-- ============================================================
-- 6. ACTIVITIES
-- ============================================================

CREATE TABLE activities (
    id 			BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    guide_day_id    BIGINT NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT NOT NULL,
    category        activity_category NOT NULL,
    address         TEXT NOT NULL,
    phone_number    VARCHAR(50),
    opening_hours   TEXT,
    website         TEXT,
    visit_order     INTEGER NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_activities_guide_day
        FOREIGN KEY (guide_day_id)
        REFERENCES guide_days (id)
        ON DELETE CASCADE,

    CONSTRAINT ck_activities_title_not_empty
        CHECK (LENGTH(TRIM(title)) > 0),

    CONSTRAINT ck_activities_description_not_empty
        CHECK (LENGTH(TRIM(description)) > 0),

    CONSTRAINT ck_activities_address_not_empty
        CHECK (LENGTH(TRIM(address)) > 0),

    CONSTRAINT ck_activities_visit_order
        CHECK (visit_order >= 1),

    CONSTRAINT uq_activities_day_visit_order
        UNIQUE (guide_day_id, visit_order)
        DEFERRABLE INITIALLY DEFERRED
);


-- ============================================================
-- 7. GUIDE INVITATIONS
-- ============================================================

CREATE TABLE guide_invitations (
    guide_id        BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    invited_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    invited_by_id   BIGINT,

    PRIMARY KEY (guide_id, user_id),

    CONSTRAINT fk_guide_invitations_guide
        FOREIGN KEY (guide_id)
        REFERENCES guides (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_guide_invitations_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_guide_invitations_inviter
        FOREIGN KEY (invited_by_id)
        REFERENCES users (id)
        ON DELETE SET NULL
);

-- Supports retrieving all guides accessible to a user.
CREATE INDEX idx_guide_invitations_user
    ON guide_invitations (user_id, guide_id);

CREATE INDEX idx_guide_invitations_inviter
    ON guide_invitations (invited_by_id);


-- ============================================================
-- 8. DATABASE INVARIANTS
-- ============================================================

-- The following aggregate-level invariants are enforced
-- transactionally by the application service layer:
--
-- 1. Every guide must have at least one day.
-- 2. Every guide must have at least one transport.
-- 3. Every guide must have at least one season.
-- 4. Every guide must have at least one audience.
-- 5. Guide day numbers must be consecutive, starting at 1.
--
-- The following invariants are enforced directly by
-- database constraints:
--
-- 1. Guide day numbers must be positive and unique per guide.
-- 2. Activity visit orders must be positive and unique per day.
-- 3. Required fields must not be null or empty.
-- 4. Foreign key relationships must remain valid.
--
-- Guide creation and its initial days/options must be persisted
-- within one transaction.
--
-- Guide updates must preserve all aggregate-level invariants.
--
-- Hibernate must validate the schema, not create or update it.