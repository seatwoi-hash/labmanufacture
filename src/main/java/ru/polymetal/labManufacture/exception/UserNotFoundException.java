package ru.polymetal.labManufacture.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super("Пользователь не найден: " + username);
    }
}
