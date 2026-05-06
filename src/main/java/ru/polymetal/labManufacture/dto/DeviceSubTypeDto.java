package ru.polymetal.labManufacture.dto;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Builder
public record DeviceSubTypeDto(String name,
                               String description,
                               Integer snType,
                               Integer versionType,
                               Boolean isInstallationOne,
                               Boolean isTestTwo,
                               Boolean isSideTwo) {

}
