package ru.polymetal.labManufacture.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.data.repository.DeviceRepository;
import ru.polymetal.labManufacture.data.repository.OperationStatusRepository;
import ru.polymetal.labManufacture.dto.DeviceDto;
import ru.polymetal.labManufacture.service.DeviceService;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.DeviceTypeService;
import ru.polymetal.labManufacture.service.OperationService;
import java.util.UUID;

@Slf4j
@Service
public class DeviceServiceImpl implements DeviceService {
    private static final String BOARD_TYPE_NAME = "BOARD";

    private final DeviceRepository deviceRepository;
    private final OperationService operationService;

    private final AccountRepository accountRepository;
    private final DeviceTypeService deviceTypeService;
    private final DeviceSubTypeService deviceSubTypeService;
    private final OperationStatusRepository deviceStatusRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository, OperationService operationService, AccountRepository accountRepository, DeviceTypeService deviceTypeService, DeviceSubTypeService deviceSubTypeService, OperationStatusRepository deviceStatusRepository) {
        this.deviceRepository = deviceRepository;
        this.operationService = operationService;
        this.accountRepository = accountRepository;
        this.deviceTypeService = deviceTypeService;
        this.deviceSubTypeService = deviceSubTypeService;
        this.deviceStatusRepository = deviceStatusRepository;
    }

    @Override
    @Transactional
    public void createDevice(DeviceDto deviceDto, String username) {
        log.info("Создание устройства пользователем: {}", username);

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        validateDeviceDto(deviceDto);

        DeviceSubType subtype = deviceSubTypeService.findById(deviceDto.getSubType().getId())
                .orElseThrow(() -> new IllegalArgumentException("Подтип не найден"));

        if (deviceRepository.existsBySerialNumber(deviceDto.getSerialNumber())) {
            throw new IllegalArgumentException(
                    String.format("Устройство с серийным номером '%s' уже существует", deviceDto.getSerialNumber()));
        }

        Device device = buildDevice(deviceDto, subtype);

        deviceRepository.save(device);

        operationService.createNewOperation(device, account, deviceDto.getDescription());

        log.info("Устройство успешно создано с ID: {}", device.getId());
    }

    @Override
    public boolean existsSerialNumber(String sn) {
        return deviceRepository.existsBySerialNumberAndIsDeletedFalse(sn);
    }


    @Override
    public void validateDeviceDto(DeviceDto deviceDto) {
        if (deviceDto.getSerialNumber() == null || deviceDto.getSerialNumber().isBlank()) {
            throw new IllegalArgumentException("Серийный номер не может быть пустым");
        }
    }

    @Override
    public void delete(UUID id) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new RuntimeException("Устройство не найдено"));
        device.setIsDeleted(true);
        deviceRepository.save(device);
    }

    public Device buildDevice(DeviceDto deviceDto, DeviceSubType subtype) {

        Device device = new Device();

        device.setSubtype(subtype);
        device.setType(deviceTypeService.findByName(BOARD_TYPE_NAME));
        device.setSerialNumber(deviceDto.getSerialNumber());

        return device;
    }
}
