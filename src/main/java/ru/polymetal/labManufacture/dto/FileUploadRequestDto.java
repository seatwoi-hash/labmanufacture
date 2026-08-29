package ru.polymetal.labManufacture.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;

/**
 * Объект передачи данных FileUploadRequestDto.
 *
 * @author Tatarinov Anton
 */
@Data
public class FileUploadRequestDto {

    @NotNull(message = "operations обязателен")
    private Operation operation;

    @NotNull(message = "account обязателен")
    private Account account;

}
