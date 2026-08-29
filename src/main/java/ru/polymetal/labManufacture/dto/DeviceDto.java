package ru.polymetal.labManufacture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import java.time.LocalDateTime;

/**
 * Объект передачи данных DeviceDto.
 *
 * @author Tatarinov Anton
 */
@Data
public class DeviceDto {

    @Setter
    @Getter
    @NotBlank(message = "Серийный номер обязателен")
    @Size(max = 100, message = "Серийный номер не более 100 символов")
    private String serialNumber;

    @Setter
    @Getter
    @NotNull(message = "Тип платы обязателен")
    private DeviceSubType subType;

    private String status;

    private LocalDateTime createdTime;

    private String description;

}
