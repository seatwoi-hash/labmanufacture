package ru.polymetal.labManufacture.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;

@Data
public class FileUploadRequestDto {

    @NotNull(message = "operations обязателен")
    private Operation operation;

    @NotNull(message = "account обязателен")
    private Account account;

}
