package ru.polymetal.labManufacture.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.repository.OperationStatusRepository;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import java.util.List;

/**
 * Реализация сервиса DeviceStatusServiceImpl.
 *
 * @author Tatarinov Anton
 */
@Service
@Slf4j
public class DeviceStatusServiceImpl implements DeviceStatusService {
    private final OperationStatusRepository deviceStatusRepository;

    public DeviceStatusServiceImpl(OperationStatusRepository deviceStatusRepository) {
        this.deviceStatusRepository = deviceStatusRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public OperationStatus findByName(String name) {


        return deviceStatusRepository.findByNameWithDevices(name)
                .orElseThrow(() -> new RuntimeException("Статус устройства '" + name + "' не найден"));
    }

    @Transactional(readOnly = true)
    public List<OperationStatus> findByListName(List<String> name) {

        return deviceStatusRepository.findByNames(name);    }

}
