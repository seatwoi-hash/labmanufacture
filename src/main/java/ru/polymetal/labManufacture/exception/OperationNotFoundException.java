package ru.polymetal.labManufacture.exception;

public class OperationNotFoundException extends RuntimeException {
    public OperationNotFoundException() {
        super("Операция не найдена");
    }

}
