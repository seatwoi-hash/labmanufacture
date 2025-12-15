package ru.polymetal.labManufacture.service;

import ru.polymetal.labManufacture.data.models.DeviceType;

public interface DeviceTypeService {

    DeviceType findByName(String name);

}
