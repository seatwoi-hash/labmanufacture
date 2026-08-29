package ru.polymetal.labManufacture.service;

import ru.polymetal.labManufacture.data.models.DeviceType;

/**
 * Контракт сервиса DeviceTypeService.
 *
 * @author Tatarinov Anton
 */
public interface DeviceTypeService {

    DeviceType findByName(String name);

}
