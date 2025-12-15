package ru.polymetal.labManufacture.service;

import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.dto.DeviceSubTypeDto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceSubTypeService {
    DeviceSubType findByName(String name);
    Optional<DeviceSubType> findById(UUID id);

    List<DeviceSubType> findAll();

    void save(String name,  String description);
    void delete(UUID id);
}
