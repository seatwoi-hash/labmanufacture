package ru.polymetal.labManufacture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record FrontAssemblyTypeDto(
        @NotBlank(message = "Имя обязательно")
        @Size(max = 100, message = "Имя не должно превышать 100 символов")
        String name,
        @NotBlank(message = "Описание обязательно")
        String description,
        @NotNull(message = "Выберите тип материнской платы")
        UUID motherboardTypeId,
        @NotNull(message = "Выберите тип платы клавиатуры")
        UUID keyboardBoardTypeId) {
}
