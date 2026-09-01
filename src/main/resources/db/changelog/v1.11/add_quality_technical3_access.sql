--liquibase formatted sql

--changeset Tatarinov A:069
--comment Grant quality role access to Technical3 operation status
INSERT INTO role_operation_status_access (role_id, operation_status_id)
SELECT role.id, operation_status.id
FROM roles role
JOIN operation_statuses operation_status ON operation_status.name = 'Technical3'
WHERE LOWER(role.name) = 'quality'
ON CONFLICT (role_id, operation_status_id) DO NOTHING;

--rollback DELETE FROM role_operation_status_access access
--rollback USING roles role, operation_statuses operation_status
--rollback WHERE access.role_id = role.id
--rollback   AND access.operation_status_id = operation_status.id
--rollback   AND LOWER(role.name) = 'quality'
--rollback   AND operation_status.name = 'Technical3';
