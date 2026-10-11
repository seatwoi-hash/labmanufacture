--liquibase formatted sql

--changeset Tatarinov A:078
--comment Add explicit failure operations for front assembly quality checks and tests
INSERT INTO front_assembly_statuses (name, description)
VALUES ('Fail_quality_check_1', 'ОТК №1 не пройден'),
       ('Fail_quality_check_2', 'ОТК №2 не пройден'),
       ('Fail_quality_check_3', 'ОТК №3 не пройден'),
       ('Fail_test_1', 'Тест №1 не пройден'),
       ('Fail_test_2', 'Тест №2 не пройден')
ON CONFLICT (name) DO NOTHING;

WITH failed_destinations AS (
    SELECT operation.*,
           previous_status.name AS source_status_name
    FROM front_assembly_operations operation
    JOIN LATERAL (
        SELECT previous_operation.status_id
        FROM front_assembly_operations previous_operation
        WHERE previous_operation.front_assembly_id = operation.front_assembly_id
          AND previous_operation.created_time < operation.created_time
        ORDER BY previous_operation.created_time DESC
        LIMIT 1
    ) previous_operation ON TRUE
    JOIN front_assembly_statuses previous_status
      ON previous_status.id = previous_operation.status_id
    JOIN front_assembly_statuses current_status
      ON current_status.id = operation.status_id
    WHERE operation.transition_action = 'failed'
      AND current_status.name NOT LIKE 'Fail_%'
), mapped_failures AS (
    SELECT failed_destinations.*,
           CASE source_status_name
               WHEN 'Quality_check_1' THEN 'Fail_quality_check_1'
               WHEN 'Quality_check_2' THEN 'Fail_quality_check_2'
               WHEN 'Quality_check_3' THEN 'Fail_quality_check_3'
               WHEN 'Test1' THEN 'Fail_test_1'
               WHEN 'Test2' THEN 'Fail_test_2'
           END AS failure_status_name
    FROM failed_destinations
)
INSERT INTO front_assembly_operations (
    id, front_assembly_id, account_id, status_id, description,
    transition_action, created_time, is_deleted, deleted_at,
    is_rollback, rolled_back_operation_id
)
SELECT uuid_generate_v4(), mapped.front_assembly_id, mapped.account_id,
       failure_status.id, mapped.description, 'failed',
       mapped.created_time - INTERVAL '1 microsecond', TRUE, mapped.created_time,
       FALSE, NULL
FROM mapped_failures mapped
JOIN front_assembly_statuses failure_status
  ON failure_status.name = mapped.failure_status_name
WHERE mapped.failure_status_name IS NOT NULL;

UPDATE front_assembly_operations operation
SET transition_action = NULL,
    description = NULL
FROM front_assembly_statuses status
WHERE operation.status_id = status.id
  AND operation.transition_action = 'failed'
  AND status.name NOT LIKE 'Fail_%';

--rollback UPDATE front_assembly_operations destination SET transition_action = 'failed', description = failure.description FROM front_assembly_operations failure JOIN front_assembly_statuses failure_status ON failure_status.id = failure.status_id WHERE failure_status.name LIKE 'Fail_%' AND destination.front_assembly_id = failure.front_assembly_id AND destination.created_time = failure.created_time + INTERVAL '1 microsecond';
--rollback DELETE FROM front_assembly_operations operation USING front_assembly_statuses status WHERE operation.status_id = status.id AND status.name IN ('Fail_quality_check_1', 'Fail_quality_check_2', 'Fail_quality_check_3', 'Fail_test_1', 'Fail_test_2');
--rollback DELETE FROM front_assembly_statuses WHERE name IN ('Fail_quality_check_1', 'Fail_quality_check_2', 'Fail_quality_check_3', 'Fail_test_1', 'Fail_test_2');
