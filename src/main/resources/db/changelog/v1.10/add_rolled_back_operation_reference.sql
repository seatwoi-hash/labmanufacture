--liquibase formatted sql

--changeset Tatarinov A:068
--comment Add reference from rollback operation to the cancelled operation
ALTER TABLE operations
    ADD COLUMN rolled_back_operation_id UUID;

ALTER TABLE operations
    ADD CONSTRAINT fk_operations_rolled_back_operation
        FOREIGN KEY (rolled_back_operation_id) REFERENCES operations (id);

CREATE INDEX idx_operations_rolled_back_operation_id
    ON operations (rolled_back_operation_id);

-- For an author's cancellation, link the rollback to the last visible operation.
UPDATE operations rollback_operation
SET rolled_back_operation_id = (
    SELECT source_operation.id
    FROM operations source_operation
    JOIN operation_statuses source_status ON source_status.id = source_operation.status_id
    WHERE source_operation.device_id = rollback_operation.device_id
      AND source_operation.created_time < rollback_operation.created_time
      AND COALESCE(source_operation.is_rollback, FALSE) = FALSE
      AND source_status.name NOT IN ('Technical', 'Technical2', 'Technical3')
    ORDER BY source_operation.created_time DESC
    LIMIT 1
)
WHERE rollback_operation.is_rollback = TRUE
  AND rollback_operation.rolled_back_operation_id IS NULL
  AND rollback_operation.description =
      'Возвращён - Отмена последней операции автором';

-- For administrative and legacy rollbacks, link to the latest preceding operation.
UPDATE operations rollback_operation
SET rolled_back_operation_id = (
    SELECT source_operation.id
    FROM operations source_operation
    WHERE source_operation.device_id = rollback_operation.device_id
      AND source_operation.created_time < rollback_operation.created_time
      AND COALESCE(source_operation.is_rollback, FALSE) = FALSE
    ORDER BY source_operation.created_time DESC
    LIMIT 1
)
WHERE rollback_operation.is_rollback = TRUE
  AND rollback_operation.rolled_back_operation_id IS NULL;

--rollback DROP INDEX idx_operations_rolled_back_operation_id;
--rollback ALTER TABLE operations DROP CONSTRAINT fk_operations_rolled_back_operation;
--rollback ALTER TABLE operations DROP COLUMN rolled_back_operation_id;
