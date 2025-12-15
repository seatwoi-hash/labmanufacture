package ru.polymetal.labManufacture.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Data
public class ProfileUpdateDto {

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    private String currentPassword;

    private String newPassword;

    private String confirmPassword;

}
