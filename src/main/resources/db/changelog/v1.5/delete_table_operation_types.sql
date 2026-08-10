--liquibase formatted sql

--changeset Tatarinov A:056
--comment Drop table operation_types
DROP TABLE IF EXISTS operation_types;
