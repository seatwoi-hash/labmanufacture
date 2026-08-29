package ru.polymetal.labManufacture.dto;

import lombok.Builder;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import java.util.UUID;

@Builder
public record DeviceSubTypeDto(UUID id,
                               @NotBlank(message = "Название типа платы обязательно")
                               @Size(max = 100, message = "Название не должно превышать 100 символов")
                               String name,
                               @NotBlank(message = "Описание типа платы обязательно") String description,
                               @NotNull(message = "Номер типа платы обязателен")
                               @Min(value = 10, message = "Номер типа должен быть не меньше 10")
                               @Max(value = 99, message = "Номер типа должен быть не больше 99") Integer snType,
                               @NotNull(message = "Версия типа платы обязательна")
                               @Min(value = 0, message = "Версия типа должна быть не меньше 0")
                               @Max(value = 9, message = "Версия типа должна быть не больше 9") Integer versionType,
                               Boolean isInstallationOne,
                               Boolean isTestTwo,
                               Boolean isSideTwo) {

    public static DeviceSubTypeDto from(DeviceSubType subtype) {
        return DeviceSubTypeDto.builder()
                .id(subtype.getId()).name(subtype.getName()).description(subtype.getDescription())
                .snType(subtype.getSnType()).versionType(subtype.getVersionType())
                .isInstallationOne(subtype.getIsInstallationOne()).isTestTwo(subtype.getIsTestTwo())
                .isSideTwo(subtype.getIsSideTwo()).build();
    }
}
