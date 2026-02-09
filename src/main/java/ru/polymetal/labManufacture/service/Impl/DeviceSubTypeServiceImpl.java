package ru.polymetal.labManufacture.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.DeviceSubTypeRepository;
import ru.polymetal.labManufacture.dto.DeviceSubTypeDto;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class DeviceSubTypeServiceImpl implements DeviceSubTypeService {
    private final DeviceSubTypeRepository deviceSubTypeRepository;

    public DeviceSubTypeServiceImpl(DeviceSubTypeRepository deviceSubTypeRepository) {
        this.deviceSubTypeRepository = deviceSubTypeRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public DeviceSubType findByName(String name) {
        log.debug("Поиск типа устройства по названию: {}", name);
        return deviceSubTypeRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Тип устройства '" + name + "' не найден"));
    }

    @Override
    public Optional<DeviceSubType> findById(UUID id) {
        return deviceSubTypeRepository.findById(id);
    }

    @Override
    public List<DeviceSubType> findAll() {

        return deviceSubTypeRepository.findAll().stream().filter(a -> !a.getIsDeleted()).toList();
    }

    @Override
    public void save(String name, String description, Boolean isInstallationOne, Boolean isTestTwo) {

        DeviceSubType deviceSubType = new DeviceSubType();
        deviceSubType.setName(name);

        deviceSubType.setDescription(description);
        deviceSubType.setIsInstallationOne(isInstallationOne);
        deviceSubType.setIsTestTwo(isTestTwo);

        if(deviceSubTypeRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Такой тип уже существует");
        }

        deviceSubTypeRepository.save(deviceSubType);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        DeviceSubType deviceSubType = deviceSubTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тип платы не найден"));

        deviceSubType.setIsDeleted(true);
    }

    @Override
    @Transactional
    public void edite(DeviceSubTypeDto deviceSubTypeDto, UUID id) {
        DeviceSubType deviceSubType = deviceSubTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тип не найден"));

        deviceSubType.setIsTestTwo(deviceSubTypeDto.isTestTwo());
        deviceSubType.setIsInstallationOne(deviceSubTypeDto.isInstallationOne());
        deviceSubType.setName(deviceSubTypeDto.name());
        deviceSubType.setDescription(deviceSubTypeDto.description());

        deviceSubTypeRepository.save(deviceSubType);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean findIsInstallationOneById(Operation operation) {
        UUID id = operation.getDevice().getSubtype().getId();
        return deviceSubTypeRepository.findIsInstallationOneByIdAndNotDeleted(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean findIsTestTwoById(Operation operation) {
        UUID id = operation.getDevice().getSubtype().getId();
        return deviceSubTypeRepository.findIsTestTwoByIdAndNotDeleted(id);
    }

}
