--liquibase formatted sql

--changeset Tatarinov A:001
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
--rollback DROP EXTENSION IF EXISTS "uuid-ossp" CASCADE;

--changeset Tatarinov A:002
CREATE TABLE accounts
(
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username      VARCHAR(50) UNIQUE  NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    first_name    VARCHAR(100)        NOT NULL,
    middle_name   VARCHAR(100),
    last_name     VARCHAR(100)        NOT NULL,
    is_active     BOOLEAN          DEFAULT TRUE,
    is_verified   BOOLEAN          DEFAULT FALSE,
    created_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    last_login    TIMESTAMP           NULL
);
--rollback DROP TABLE accounts CASCADE;

--changeset Tatarinov A:003
CREATE INDEX idx_accounts_username ON accounts (username);
CREATE INDEX idx_accounts_email ON accounts (email);
CREATE INDEX idx_accounts_is_active ON accounts (is_active);
CREATE INDEX idx_accounts_created_at ON accounts (created_at);
--rollback DROP INDEX idx_accounts_username;
--rollback DROP INDEX idx_accounts_email;
--rollback DROP INDEX idx_accounts_is_active;
--rollback DROP INDEX idx_accounts_created_at;

--changeset Tatarinov A:004
--comment Add default admin user
INSERT INTO accounts (username, email, password_hash, first_name, middle_name, last_name, is_active, is_verified)
VALUES ('root',
        'grabezhevig@polymetal.ru',
        '$2a$12$td5mvXyPQ2/GdHPgnD94gupLwcbv/RulPzN10GHaCkjxnY8a1sl0a',
        'Игорь',
        'Геннадьевич',
        'Грабежев',
        TRUE,
        TRUE);
--rollback DELETE FROM accounts WHERE username = 'admin';
