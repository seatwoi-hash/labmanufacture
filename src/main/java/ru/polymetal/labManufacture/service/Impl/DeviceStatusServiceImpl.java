package ru.polymetal.labManufacture.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.polymetal.labManufacture.data.models.DeviceStatus;
import ru.polymetal.labManufacture.data.repository.DeviceStatusRepository;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import java.util.List;

@Service
@Slf4j
public class DeviceStatusServiceImpl implements DeviceStatusService {
    private final DeviceStatusRepository deviceStatusRepository;

    public DeviceStatusServiceImpl(DeviceStatusRepository deviceStatusRepository) {
        this.deviceStatusRepository = deviceStatusRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceStatus findByName(String name) {

        //log.debug("Поиск статуса по названию: {}", name);

        return deviceStatusRepository.findByNameWithDevices(name)
                .orElseThrow(() -> new RuntimeException("Статус устройства '" + name + "' не найден"));
    }

    @Transactional(readOnly = true)
    public List<DeviceStatus> findByListName(List<String> name) {

        //log.debug("Поиск статуса по названию: {}", name);

        return deviceStatusRepository.findByNames(name);    }

}
