package ru.polymetal.labManufacture.exception;

/**
 * Исключение приложения DeviceTypeNotFoundException.
 *
 * @author Tatarinov Anton
 */
public class DeviceTypeNotFoundException extends RuntimeException {
    public DeviceTypeNotFoundException() {
        super("Тип платы не найден");
    }
}
