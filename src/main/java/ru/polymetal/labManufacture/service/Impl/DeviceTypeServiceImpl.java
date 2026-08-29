package ru.polymetal.labManufacture.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.polymetal.labManufacture.data.models.DeviceType;
import ru.polymetal.labManufacture.data.repository.DeviceTypeRepository;
import ru.polymetal.labManufacture.service.DeviceTypeService;

/**
 * Реализация сервиса DeviceTypeServiceImpl.
 *
 * @author Tatarinov Anton
 */
@Service
@Slf4j
public class DeviceTypeServiceImpl implements DeviceTypeService {
    private final DeviceTypeRepository deviceTypeRepository;

    public DeviceTypeServiceImpl(DeviceTypeRepository deviceTypeRepository) {
        this.deviceTypeRepository = deviceTypeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceType findByName(String name) {
        log.debug("Поиск типа устройства по названию: {}", name);
        return deviceTypeRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Тип устройства '" + name + "' не найден"));
    }

}
