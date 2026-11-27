-- One-time codes that let the mobile app exchange an onboarding link for its first tokens.
-- Only the SHA-256 hash (hex) of a code is stored; the code itself is shown once, in the link.
-- A code is valid until expires_at and can be redeemed once (used_at is set on redemption or when
-- a newer link replaces it).
CREATE TABLE onboarding_code (
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL,
    code_hash   VARCHAR(64)  NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_onboarding_code_user_id ON onboarding_code (user_id);
