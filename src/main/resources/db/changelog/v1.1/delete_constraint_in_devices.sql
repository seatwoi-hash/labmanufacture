--changeset Tatarinov A:043
--comment Remove UNIQUE constraint from devices.sn
ALTER TABLE devices
    DROP CONSTRAINT IF EXISTS devices_sn_key; -- или другое имя constraint

--rollback ALTER TABLE devices
--rollback ADD CONSTRAINT devices_sn_unique UNIQUE (sn);
