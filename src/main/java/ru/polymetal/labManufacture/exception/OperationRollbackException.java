package ru.polymetal.labManufacture.exception;

/**
 * Исключение недопустимого возврата производственной операции.
 *
 * @author Tatarinov Anton
 */
public class OperationRollbackException extends RuntimeException {

    public OperationRollbackException(String message) {
        super(message);
    }
}
