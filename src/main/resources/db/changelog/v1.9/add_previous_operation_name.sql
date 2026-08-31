--liquibase formatted sql

--changeset Tatarinov A:067
--comment Add previous operation display name to status routes
ALTER TABLE operation_status_routes
    ADD COLUMN previous_operation_name VARCHAR(100);

UPDATE operation_status_routes route
SET previous_operation_name = CASE current_status.name
    WHEN 'Technical' THEN 'ОТК №2'
    WHEN 'Technical2' THEN 'Тестировка №2'
    WHEN 'Technical3' THEN 'Монтаж "Сторона 1"'
    ELSE (
        SELECT previous_status.description
        FROM operation_statuses previous_status
        WHERE previous_status.id = route.previous_status_id
    )
END
FROM operation_statuses current_status
WHERE current_status.id = route.current_status_id;

--rollback ALTER TABLE operation_status_routes DROP COLUMN previous_operation_name;
