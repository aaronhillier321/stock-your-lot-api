-- User status: PENDING (invited, no password yet) or ACTIVE
ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

ALTER TABLE users ADD CONSTRAINT chk_users_status CHECK (status IN ('PENDING', 'ACTIVE'));

CREATE INDEX idx_users_status ON users (status);

-- Invites: token_hash identifies the invite; we never store the raw token
CREATE TABLE invites (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    token_hash    VARCHAR(64) NOT NULL,
    inviter_id    UUID REFERENCES users (id) ON DELETE SET NULL,
    user_id       UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at    TIMESTAMPTZ NOT NULL,
    expires_at    TIMESTAMPTZ NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT chk_invites_status CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED'))
);

CREATE UNIQUE INDEX idx_invites_token_hash ON invites (token_hash);
CREATE INDEX idx_invites_email ON invites (email);
CREATE INDEX idx_invites_user_id ON invites (user_id);
CREATE INDEX idx_invites_expires_at ON invites (expires_at);
