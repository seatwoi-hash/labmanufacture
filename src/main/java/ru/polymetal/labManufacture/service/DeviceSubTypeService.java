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

    void save(DeviceSubTypeDto deviceSubTypeDto, MultipartFile file, MultipartFile zip) throws IOException;

    void delete(UUID id);

    void edit(DeviceSubTypeDto deviceSubTypeDto, UUID id, MultipartFile file, MultipartFile zip) throws IOException;

    @Deprecated(forRemoval = false)
    default void edite(DeviceSubTypeDto deviceSubTypeDto, UUID id, MultipartFile file, MultipartFile zip) throws IOException {
        edit(deviceSubTypeDto, id, file, zip);
    }

    Boolean findIsInstallationOneById(Operation operation);

    Boolean findIsTestTwoById(Operation operation);
    Boolean findIsSideTwoById(Operation operation);


    void uploadFile(String newName, byte[] file, UUID deviceSubTypeID) throws IOException;

}
