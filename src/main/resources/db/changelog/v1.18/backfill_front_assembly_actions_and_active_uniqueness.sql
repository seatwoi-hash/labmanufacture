--liquibase formatted sql

--changeset Tatarinov A:077
--comment Backfill failed front assembly actions and allow reuse of archived identifiers
UPDATE front_assembly_operations operation
SET transition_action = 'failed'
FROM front_assembly_statuses status
WHERE operation.status_id = status.id
  AND status.name IN ('Repair1', 'Diagnostician');

UPDATE front_assembly_operations operation
SET transition_action = 'failed'
FROM front_assembly_statuses status
WHERE operation.status_id = status.id
  AND status.name = 'Repair2'
  AND EXISTS (
    SELECT 1
    FROM front_assembly_operations previous_operation
    JOIN front_assembly_statuses previous_status ON previous_status.id = previous_operation.status_id
    WHERE previous_operation.front_assembly_id = operation.front_assembly_id
      AND previous_operation.created_time < operation.created_time
      AND previous_status.name = 'Quality_check_3'
      AND NOT EXISTS (
        SELECT 1
        FROM front_assembly_operations between_operation
        WHERE between_operation.front_assembly_id = operation.front_assembly_id
          AND between_operation.created_time > previous_operation.created_time
          AND between_operation.created_time < operation.created_time
      )
  );

ALTER TABLE front_assembly_types
    DROP CONSTRAINT IF EXISTS uq_front_assembly_types_name;
ALTER TABLE front_assemblies
    DROP CONSTRAINT IF EXISTS uq_front_assemblies_case_id;

CREATE UNIQUE INDEX uq_front_assembly_types_active_name
    ON front_assembly_types (LOWER(name)) WHERE is_deleted = false;
CREATE UNIQUE INDEX uq_front_assemblies_active_case_id
    ON front_assemblies (LOWER(case_id)) WHERE is_deleted = false;

--rollback DROP INDEX uq_front_assemblies_active_case_id;
--rollback DROP INDEX uq_front_assembly_types_active_name;
--rollback ALTER TABLE front_assemblies ADD CONSTRAINT uq_front_assemblies_case_id UNIQUE (case_id);
--rollback ALTER TABLE front_assembly_types ADD CONSTRAINT uq_front_assembly_types_name UNIQUE (name);
--rollback UPDATE front_assembly_operations SET transition_action = NULL WHERE transition_action = 'failed';
