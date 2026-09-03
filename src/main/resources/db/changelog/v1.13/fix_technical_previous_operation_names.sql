--liquibase formatted sql

--changeset Tatarinov A:071
--comment Fix previous operation names for Technical and Technical2 statuses
UPDATE operation_status_routes route
SET previous_operation_name = CASE current_status.name
    WHEN 'Technical' THEN 'ОТК №1'
    WHEN 'Technical2' THEN 'Тестировка №1'
END
FROM operation_statuses current_status
WHERE current_status.id = route.current_status_id
  AND current_status.name IN ('Technical', 'Technical2');

--rollback UPDATE operation_status_routes route
--rollback SET previous_operation_name = CASE current_status.name
--rollback     WHEN 'Technical' THEN 'ОТК №2'
--rollback     WHEN 'Technical2' THEN 'Тестировка №2'
--rollback END
--rollback FROM operation_statuses current_status
--rollback WHERE current_status.id = route.current_status_id
--rollback   AND current_status.name IN ('Technical', 'Technical2');
