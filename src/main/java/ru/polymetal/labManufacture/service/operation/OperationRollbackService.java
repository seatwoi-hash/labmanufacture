package ru.polymetal.labManufacture.service.operation;

import ru.polymetal.labManufacture.data.models.Account;

import java.util.UUID;

/**
 * Контракт возврата операции к предыдущему статусу.
 *
 * @author Tatarinov Anton
 */
public interface OperationRollbackService {

    UUID rollback(UUID operationId, Account account, String description);

    UUID rollbackTo(UUID operationId, UUID targetOperationId, Account account, String description);

    boolean canCancelOwnLastOperation(UUID operationId, Account account);

    UUID cancelOwnLastOperation(UUID operationId, Account account, String comment);
}
