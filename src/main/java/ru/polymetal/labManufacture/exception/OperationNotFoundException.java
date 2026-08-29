package ru.polymetal.labManufacture.exception;

/**
 * Исключение приложения OperationNotFoundException.
 *
 * @author Tatarinov Anton
 */
public class OperationNotFoundException extends RuntimeException {
    public OperationNotFoundException() {
        super("Операция не найдена");
    }

}
