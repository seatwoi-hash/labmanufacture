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
}
