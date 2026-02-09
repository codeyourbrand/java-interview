CREATE SCHEMA IF NOT EXISTS financial_log;

CREATE TABLE financial_log.financial_log_entry
(
    uuid            UUID           NOT NULL,
    version         BIGINT,
    sequence_number BIGINT         GENERATED ALWAYS AS IDENTITY,
    status          VARCHAR(255)   NOT NULL,
    name            VARCHAR(255)   NOT NULL,
    aed_amount      DECIMAL(19, 2) NOT NULL,
    category        VARCHAR(255)   NOT NULL,
    source          VARCHAR(255)   NOT NULL,
    settle_date     date           NOT NULL,
    notes           VARCHAR(255),
    tags            JSONB          NOT NULL,
    amount          DECIMAL(19, 2),
    currency        VARCHAR(255),
    reference_id    VARCHAR(255),
    reference_type  VARCHAR(255),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_financial_log_entry PRIMARY KEY (uuid)
);

CREATE TABLE financial_log.financial_log_history
(
    uuid               UUID                        NOT NULL,
    financial_log_uuid UUID                        NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by         VARCHAR(255)                NOT NULL,
    financial_log_name VARCHAR(255)                NOT NULL,
    settle_date        date                        NOT NULL,
    action             VARCHAR(255)                NOT NULL,
    status             VARCHAR(255)                NOT NULL,
    tags               JSONB                       NOT NULL,
    notes              VARCHAR(255),
    original_amount    DECIMAL(19, 2)              NOT NULL,
    original_currency  VARCHAR(3)                  NOT NULL,
    converted_amount   DECIMAL(19, 2)              NOT NULL,
    converted_currency VARCHAR(3)                  NOT NULL,
    CONSTRAINT pk_financial_log_history PRIMARY KEY (uuid)
);

CREATE TABLE financial_log.financial_log_tag
(
    uuid     UUID         NOT NULL,
    name     VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    CONSTRAINT pk_financial_log_tag PRIMARY KEY (uuid)
);

ALTER TABLE financial_log.financial_log_tag
    ADD CONSTRAINT uc_financial_log_tag_name UNIQUE (name);

ALTER TABLE financial_log.financial_log_history
    ADD CONSTRAINT FK_FINANCIAL_LOG_ENTRY FOREIGN KEY (financial_log_uuid) REFERENCES financial_log.financial_log_entry (uuid) ON DELETE CASCADE;

CREATE INDEX idx_financial_log_history_log_entry ON financial_log.financial_log_history (financial_log_uuid);
CREATE INDEX idx_financial_log_history_financial_log_id_created_at ON financial_log.financial_log_history (financial_log_uuid, created_at);

CREATE INDEX idx_financial_log_entry_created_at_status ON financial_log.financial_log_entry (created_at, status);
CREATE INDEX idx_financial_log_entry_settle_date ON financial_log.financial_log_entry (settle_date);
CREATE INDEX idx_financial_log_entry_aed_amount ON financial_log.financial_log_entry (aed_amount);
CREATE INDEX idx_financial_log_entry_category ON financial_log.financial_log_entry (category);
CREATE INDEX idx_financial_log_entry_tags_gin ON financial_log.financial_log_entry USING GIN (tags);
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_financial_log_entry_name_trgm ON financial_log.financial_log_entry USING GIN (lower(name) gin_trgm_ops);

CREATE TABLE financial_log.tmp_aed_rates (
     rate_date date PRIMARY KEY,
     rate      numeric(18,8) NOT NULL
);
