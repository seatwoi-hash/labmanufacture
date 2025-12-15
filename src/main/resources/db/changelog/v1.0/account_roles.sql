--liquibase formatted sql

--changeset Tatarinov A:009
--comment Create account_roles junction table
CREATE TABLE account_roles
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id  UUID NOT NULL,
    role_id     UUID NOT NULL,
    assigned_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_account_roles_account
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_account_roles_role
        FOREIGN KEY (role_id)
            REFERENCES roles (id)
            ON DELETE CASCADE,

    CONSTRAINT unique_account_role
        UNIQUE (account_id, role_id)
);
--rollback DROP TABLE account_roles CASCADE;

--changeset Tatarinov A:010
--comment Create index for account_id
CREATE INDEX idx_account_roles_account ON account_roles (account_id);
--rollback DROP INDEX idx_account_roles_account;

--changeset Tatarinov A:011
--comment Create index for role_id
CREATE INDEX idx_account_roles_role ON account_roles (role_id);
--rollback DROP INDEX idx_account_roles_role;

--changeset Tatarinov A:012
--comment Create index for assigned_at
CREATE INDEX idx_account_roles_assigned_at ON account_roles (assigned_at);
--rollback DROP INDEX idx_account_roles_assigned_at;

--changeset Tatarinov A:013
--comment Assign admin role to admin user
INSERT INTO account_roles (account_id, role_id)
SELECT (SELECT id FROM accounts WHERE username = 'root'),
       (SELECT id FROM roles WHERE name = 'admin');
--rollback DELETE FROM account_roles
--rollback WHERE account_id = (SELECT id FROM accounts WHERE username = 'admin')
--rollback AND role_id = (SELECT id FROM roles WHERE name = 'admin');
