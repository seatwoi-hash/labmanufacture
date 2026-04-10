package ru.polymetal.labManufacture.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.DeviceSubTypeRepository;
import ru.polymetal.labManufacture.dto.DeviceSubTypeDto;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.nextcloud.NextcloudService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class DeviceSubTypeServiceImpl implements DeviceSubTypeService {
    private final DeviceSubTypeRepository deviceSubTypeRepository;
    private final NextcloudService nextcloudService;

    public DeviceSubTypeServiceImpl(DeviceSubTypeRepository deviceSubTypeRepository, NextcloudService nextcloudService) {
        this.deviceSubTypeRepository = deviceSubTypeRepository;
        this.nextcloudService = nextcloudService;
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
    public void save(DeviceSubTypeDto deviceSubTypeDto, MultipartFile file) throws IOException {

        if(deviceSubTypeRepository.findByName(deviceSubTypeDto.name()).isPresent()) {
            throw new RuntimeException("Такой тип уже существует");
        }

        DeviceSubType deviceSubType = new DeviceSubType();

        if(file != null) {
            String newName = UUID.randomUUID().toString() + ".pdf";
            nextcloudService.uploadFile(newName, file.getBytes());
            deviceSubType.setFileName(newName);
        }

        deviceSubType.setName(deviceSubTypeDto.name());
        deviceSubType.setSnType(deviceSubTypeDto.snType());
        deviceSubType.setVersionType(deviceSubTypeDto.versionType());
        deviceSubType.setDescription(deviceSubTypeDto.description());
        deviceSubType.setIsInstallationOne(deviceSubTypeDto.isInstallationOne());
        deviceSubType.setIsTestTwo(deviceSubTypeDto.isTestTwo());

        deviceSubTypeRepository.save(deviceSubType);

    }

    @Override
    @Transactional
    public void delete(UUID id) {
        DeviceSubType deviceSubType = deviceSubTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тип платы не найден"));

        deviceSubTypeRepository.delete(deviceSubType);
    }

    @Override
    @Transactional
    public void edite(DeviceSubTypeDto deviceSubTypeDto, UUID id, MultipartFile file) throws IOException {
        DeviceSubType deviceSubType = deviceSubTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тип не найден"));


        if(file != null) {
            String newName = UUID.randomUUID().toString() + ".pdf";
            this.uploadFile(newName,file.getBytes());

            deviceSubType.setFileName(newName);
        }

        deviceSubType.setIsTestTwo(deviceSubTypeDto.isTestTwo());
        deviceSubType.setIsInstallationOne(deviceSubTypeDto.isInstallationOne());
        deviceSubType.setName(deviceSubTypeDto.name());
        deviceSubType.setVersionType(deviceSubTypeDto.versionType());
        deviceSubType.setSnType(deviceSubTypeDto.snType());
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

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void uploadFile(String newName, byte[] file) throws IOException {
        nextcloudService.uploadFile(newName, file);
    }


}
