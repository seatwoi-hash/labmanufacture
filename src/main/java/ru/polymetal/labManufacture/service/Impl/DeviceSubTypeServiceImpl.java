package ru.polymetal.labManufacture.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.polymetal.labManufacture.config.NextcloudConfig;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.DeviceSubTypeRepository;
import ru.polymetal.labManufacture.dto.DeviceSubTypeDto;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.nextcloud.LinkService;
import ru.polymetal.labManufacture.service.nextcloud.NextcloudService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * Реализация сервиса DeviceSubTypeServiceImpl.
 *
 * @author Tatarinov Anton
 */
@Service
@Slf4j
public class DeviceSubTypeServiceImpl implements DeviceSubTypeService {

    private final DeviceSubTypeRepository deviceSubTypeRepository;
    private final NextcloudService nextcloudService;

    private final LinkService linkService;
    private final NextcloudConfig nextcloudConfig;


    public DeviceSubTypeServiceImpl(DeviceSubTypeRepository deviceSubTypeRepository,
                                    NextcloudService nextcloudService, LinkService linkService,
                                    NextcloudConfig nextcloudConfig) {
        this.deviceSubTypeRepository = deviceSubTypeRepository;
        this.nextcloudService = nextcloudService;
        this.linkService = linkService;
        this.nextcloudConfig = nextcloudConfig;
    }


    @Override
    @Transactional(readOnly = true)
    public DeviceSubType findByName(String name) {
        log.debug("Поиск типа устройства по названию: {}", name);
        return deviceSubTypeRepository.findByNameAndIsDeletedFalse(name)
                .orElseThrow(() -> new RuntimeException("Тип устройства '" + name + "' не найден"));
    }

    @Override
    public Optional<DeviceSubType> findById(UUID id) {
        return deviceSubTypeRepository.findById(id);
    }

    @Override
    public List<DeviceSubType> findAll() {

        return deviceSubTypeRepository.findAllByIsDeletedFalse();
    }

    @Override
    @Transactional
    public void save(DeviceSubTypeDto deviceSubTypeDto, MultipartFile file, MultipartFile zip) throws IOException {

        if (deviceSubTypeRepository.findByNameAndIsDeletedFalse(deviceSubTypeDto.name().trim()).isPresent()) {
            throw new RuntimeException("Такой тип уже существует");
        }

        DeviceSubType deviceSubType = new DeviceSubType();


        deviceSubType.setName(deviceSubTypeDto.name().trim());
        deviceSubType.setSnType(deviceSubTypeDto.snType());
        deviceSubType.setVersionType(deviceSubTypeDto.versionType());
        deviceSubType.setDescription(deviceSubTypeDto.description());
        deviceSubType.setIsInstallationOne(deviceSubTypeDto.isInstallationOne());
        deviceSubType.setIsTestTwo(deviceSubTypeDto.isTestTwo());
        deviceSubType.setIsSideTwo(deviceSubTypeDto.isSideTwo());

        if (zip != null && !zip.isEmpty()) {
            deviceSubType.setData(zip.getBytes());
        }

        DeviceSubType saved = deviceSubTypeRepository.save(deviceSubType);

        if (file != null && !file.isEmpty()) {
            String newName = UUID.randomUUID() + ".pdf";
            this.uploadFile(newName, file.getBytes(), saved.getId());
            deviceSubType.setFileName(newName);
        }

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
    public void edit(DeviceSubTypeDto deviceSubTypeDto, UUID id, MultipartFile file, MultipartFile zip) throws IOException {
        DeviceSubType deviceSubType = deviceSubTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тип не найден"));

        String normalizedName = deviceSubTypeDto.name().trim();
        if (deviceSubTypeRepository.existsByNameAndIdNot(normalizedName, id)) {
            throw new RuntimeException("Такой тип уже существует");
        }

        deviceSubType.setIsTestTwo(deviceSubTypeDto.isTestTwo());
        deviceSubType.setIsInstallationOne(deviceSubTypeDto.isInstallationOne());
        deviceSubType.setIsSideTwo(deviceSubTypeDto.isSideTwo());
        deviceSubType.setName(normalizedName);
        deviceSubType.setVersionType(deviceSubTypeDto.versionType());
        deviceSubType.setSnType(deviceSubTypeDto.snType());
        deviceSubType.setDescription(deviceSubTypeDto.description());

        if (zip != null && !zip.isEmpty()) {
            deviceSubType.setData(zip.getBytes());
        }

        DeviceSubType saved = deviceSubTypeRepository.save(deviceSubType);

        if (file != null && !file.isEmpty()) {
            String newName = UUID.randomUUID() + ".pdf";
            this.uploadFile(newName, file.getBytes(), saved.getId());
            deviceSubType.setFileName(newName);
        }
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
    public Boolean findIsSideTwoById(Operation operation) {
        UUID id = operation.getDevice().getSubtype().getId();
        return deviceSubTypeRepository.findIsSideTwoByIdAndNotDeleted(id);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void uploadFile(String newName, byte[] file, UUID deviceSubTypeID) throws IOException {


        if (file.length > 0) {
            nextcloudService.uploadFile(newName, file);

            DeviceSubType deviceSubType = deviceSubTypeRepository.findById(deviceSubTypeID).orElseThrow(
                    () -> new RuntimeException("Тип платы не найден")
            );

            linkService.createPublicShareDeviceSubType(
                    newName,
                    deviceSubType
            );
        } else {
            throw new RuntimeException("Файл пустой " + newName);
        }
    }

}
