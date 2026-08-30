--liquibase formatted sql

--changeset Tatarinov A:060
--comment Create role to operation status access table
CREATE TABLE role_operation_status_access
(
    role_id             UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    operation_status_id UUID NOT NULL REFERENCES operation_statuses (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, operation_status_id)
);

CREATE INDEX idx_role_operation_status_access_status
    ON role_operation_status_access (operation_status_id);
--rollback DROP TABLE role_operation_status_access;

--changeset Tatarinov A:061
--comment Migrate role to operation status access matrix
WITH access_matrix(role_name, status_name) AS (
    VALUES
        ('operator', 'created'),
        ('operator', 'Side1'),
        ('diagnostician', 'Fail_test'),
        ('diagnostician', 'Fail_test2'),
        ('quality', 'Side2'),
        ('quality', 'Repair1'),
        ('quality', 'Repair2'),
        ('quality', 'Repair3'),
        ('quality', 'Repair4'),
        ('quality', 'Repair5'),
        ('quality', 'Repair6'),
        ('quality', 'Installation1'),
        ('quality', 'Installation2'),
        ('quality', 'Washing1'),
        ('quality', 'Washing2'),
        ('quality', 'Varnish'),
        ('output', 'Quality_check_№1'),
        ('output', 'Quality_check_№1.1'),
        ('output', 'Test'),
        ('repairman', 'Fail_quality_check_№1'),
        ('repairman', 'Fail_quality_check_№1.1'),
        ('repairman', 'Fail_quality_check_№2'),
        ('repairman', 'Fail_quality_check_№2.1'),
        ('repairman', 'Fail_quality_check_№3'),
        ('repairman', 'Fail_quality_check_№4'),
        ('repairman', 'Fail_quality_check_№4.1'),
        ('repairman', 'Fail_quality_check_№4.2'),
        ('repairman', 'Fail_quality_check_№5'),
        ('repairman', 'Fail_quality_check_№5.1'),
        ('repairman', 'Fail_test'),
        ('repairman', 'Fail_test2'),
        ('washer', 'Test2'),
        ('washer', 'Quality_check_№5.1'),
        ('washer', 'Technical2'),
        ('washer', 'Fail_quality_check_№5.1.1'),
        ('varnisher', 'Quality_check_№5'),
        ('varnisher', 'Quality_check_№5.1.1'),
        ('varnisher', 'Fail_quality_check_№6'),
        ('testerb', 'Quality_check_№2'),
        ('testerb', 'Quality_check_№2.1'),
        ('testerb', 'Quality_check_№3'),
        ('testerb', 'Quality_check_№4'),
        ('testerb', 'Quality_check_№4.1'),
        ('testerb', 'Quality_check_№4.2'),
        ('testerb', 'Technical'),
        ('user', 'created')
)
INSERT INTO role_operation_status_access (role_id, operation_status_id)
SELECT r.id, s.id
FROM access_matrix m
JOIN roles r ON lower(r.name) = m.role_name
JOIN operation_statuses s ON s.name = m.status_name
ON CONFLICT (role_id, operation_status_id) DO NOTHING;
--rollback DELETE FROM role_operation_status_access;
