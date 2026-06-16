package ru.polymetal.labManufacture.exception;

public class DeviceTypeNotFoundException extends RuntimeException {
    public DeviceTypeNotFoundException() {
        super("Тип платы не найден");
    }
}
