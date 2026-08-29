package ru.polymetal.labManufacture.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Объект передачи данных AccountCreateFormDto.
 *
 * @author Tatarinov Anton
 */
@Setter
@Getter
public class AccountCreateFormDto {

    private UUID id;
    @NotBlank(message = "Логин не может быть пустым")
    @Size(min = 3, max = 50, message = "Логин должен содержать от 3 до 50 символов")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Логин может содержать только буквы, цифры, точки, дефисы и подчеркивания")
    private String username;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    @Size(max = 50, message = "Имя должно содержать не более 50 символов")
    private String firstName;

    @Size(max = 50, message = "Отчество должно содержать не более 50 символов")
    private String middleName;

    @Size(max = 50, message = "Фамилия должна содержать не более 50 символов")
    private String lastName;

    private String password;

    private String confirmPassword;

    private Set<String> roles;
    public boolean isPasswordPresent() {
        return password != null && !password.trim().isEmpty();
    }


    public AccountCreateFormDto() {}

}
