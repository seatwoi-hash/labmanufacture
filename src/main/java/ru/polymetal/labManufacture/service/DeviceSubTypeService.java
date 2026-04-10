package ru.polymetal.labManufacture.service;

import org.springframework.web.multipart.MultipartFile;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.dto.DeviceSubTypeDto;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceSubTypeService {
    DeviceSubType findByName(String name);
    Optional<DeviceSubType> findById(UUID id);

    List<DeviceSubType> findAll();

    void save(DeviceSubTypeDto deviceSubTypeDto, MultipartFile file) throws IOException;
    void delete(UUID id);
    void edite(DeviceSubTypeDto deviceSubTypeDto, UUID id, MultipartFile file) throws IOException;

    Boolean findIsInstallationOneById(Operation operation);
    Boolean findIsTestTwoById(Operation operation);

    void uploadFile(String newName, byte[] file) throws IOException;
}
