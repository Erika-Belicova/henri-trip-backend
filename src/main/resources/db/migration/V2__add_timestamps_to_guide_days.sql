-- Standardise audit timestamps across persistent entities.

ALTER TABLE guide_days
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE guide_invitations
    RENAME COLUMN invited_at TO created_at;

ALTER TABLE guide_invitations
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;