--liquibase formatted sql

--changeset Tatarinov A:058
--comment add value table role
INSERT INTO roles (id, name, description,  is_default)
VALUES (uuid_generate_v4(), 'developer', 'Разработчик', FALSE);
--rollback DELETE FROM roles WHERE name = 'developer';
