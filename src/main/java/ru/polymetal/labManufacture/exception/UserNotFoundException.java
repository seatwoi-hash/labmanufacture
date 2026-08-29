package ru.polymetal.labManufacture.exception;

/**
 * Исключение приложения UserNotFoundException.
 *
 * @author Tatarinov Anton
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super("Пользователь не найден: " + username);
    }
}
