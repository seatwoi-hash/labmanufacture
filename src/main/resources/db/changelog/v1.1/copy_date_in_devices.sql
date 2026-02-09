--liquibase formatted sql

--changeset Tatarinov A:036
--comment Copy device data from operations to devices table with new UUIDs
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM devices

WITH ranked_operations AS (
    SELECT
        sn,
        type_id,
        subtype_id,
        is_deleted,
        deleted_at,
        created_time,
        ROW_NUMBER() OVER (PARTITION BY sn ORDER BY created_time DESC) as rn
    FROM operations
),
     unique_devices AS (
         SELECT
             sn,
             type_id,
             subtype_id,
             is_deleted,
             deleted_at
         FROM ranked_operations
         WHERE rn = 1
     )
INSERT INTO devices (id, sn, type_id, subtype_id, is_deleted, deleted_at)
SELECT
    uuid_generate_v4(),
    sn,
    type_id,
    subtype_id,
    is_deleted,
    deleted_at
FROM unique_devices;

--rollback TRUNCATE TABLE devices;
