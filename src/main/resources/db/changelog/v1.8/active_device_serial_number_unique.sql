--liquibase formatted sql

--changeset Tatarinov A:065
CREATE UNIQUE INDEX uk_devices_active_sn
    ON devices (sn)
    WHERE is_deleted = FALSE;

--rollback DROP INDEX IF EXISTS uk_devices_active_sn;
