create TABLE accounts (
    id             UUID         PRIMARY KEY,
    email          VARCHAR(254) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    email_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uq_accounts_email UNIQUE (email)
);


create TABLE email_confirmations (
    id         UUID        PRIMARY KEY,
    account_id UUID        NOT NULL REFERENCES accounts (id) ON delete CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_email_confirmations_token UNIQUE (token_hash)
);

create index ix_email_confirmations_account_created
    on email_confirmations (account_id, created_at desc);


create TABLE sessions (
    id                          UUID        PRIMARY KEY,
    account_id                  UUID        NOT NULL REFERENCES accounts (id) ON delete CASCADE,
    refresh_token_hash          VARCHAR(64) NOT NULL,
    previous_refresh_token_hash VARCHAR(64),
    expires_at                  TIMESTAMPTZ NOT NULL,
    revoked_at                  TIMESTAMPTZ,
    created_at                  TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_sessions_refresh_token UNIQUE (refresh_token_hash)
);

create index ix_sessions_previous_token
    on sessions (previous_refresh_token_hash)
    WHERE previous_refresh_token_hash IS NOT NULL;

create index ix_sessions_account_active
    on sessions (account_id)
    WHERE revoked_at IS NULL;


create TABLE password_resets (
    id         UUID        PRIMARY KEY,
    account_id UUID        NOT NULL REFERENCES accounts (id) ON delete CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_password_resets_token UNIQUE (token_hash)
);

create index ix_password_resets_account_created
    on password_resets (account_id, created_at desc);


create TABLE outbox_messages (
    id              UUID          PRIMARY KEY,
    type            VARCHAR(64)   NOT NULL,
    aggregate_id    UUID,
    payload         JSONB         NOT NULL,
    attempts        INT           NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ   NOT NULL,
    last_error      VARCHAR(1000),
    dead_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL
);

create index ix_outbox_ready
    on outbox_messages (next_attempt_at)
    WHERE dead_at IS NULL;