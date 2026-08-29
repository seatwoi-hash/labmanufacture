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

        log.info("Начато создание типа платы: name={}, pdfSize={}, archiveSize={}",
                deviceSubTypeDto.name(), fileSize(file), fileSize(zip));

        if (deviceSubTypeRepository.findByNameAndIsDeletedFalse(deviceSubTypeDto.name().trim()).isPresent()) {
            log.warn("Отклонено создание типа платы: имя уже существует, name={}", deviceSubTypeDto.name());
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
        log.info("Тип платы создан: subtypeId={}, name={}", saved.getId(), saved.getName());

        if (file != null && !file.isEmpty()) {
            String newName = UUID.randomUUID() + ".pdf";
            this.uploadFile(newName, file.getBytes(), saved.getId());
            deviceSubType.setFileName(newName);
        }

    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Начато удаление типа платы: subtypeId={}", id);
        DeviceSubType deviceSubType = deviceSubTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тип платы не найден"));

        deviceSubType.setIsDeleted(true);
        log.info("Тип платы помечен удалённым: subtypeId={}, name={}", id, deviceSubType.getName());
    }

    @Override
    @Transactional
    public void edit(DeviceSubTypeDto deviceSubTypeDto, UUID id, MultipartFile file, MultipartFile zip) throws IOException {
        log.info("Начато редактирование типа платы: subtypeId={}, name={}, pdfSize={}, archiveSize={}",
                id, deviceSubTypeDto.name(), fileSize(file), fileSize(zip));
        DeviceSubType deviceSubType = deviceSubTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тип не найден"));

        String normalizedName = deviceSubTypeDto.name().trim();
        if (deviceSubTypeRepository.existsByNameAndIdNot(normalizedName, id)) {
            log.warn("Отклонено редактирование типа платы: имя уже существует, subtypeId={}, name={}", id, normalizedName);
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
        log.info("Тип платы обновлён: subtypeId={}, name={}", saved.getId(), saved.getName());

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
        log.info("Начата загрузка PDF типа платы в Nextcloud: subtypeId={}, storedName={}, size={}",
                deviceSubTypeID, newName, file.length);
        if (file.length > 0) {
            nextcloudService.uploadFile(newName, file);

            DeviceSubType deviceSubType = deviceSubTypeRepository.findById(deviceSubTypeID).orElseThrow(
                    () -> new RuntimeException("Тип платы не найден")
            );

            linkService.createPublicShareDeviceSubType(
                    newName,
                    deviceSubType
            );
            log.info("PDF типа платы загружен в Nextcloud: subtypeId={}, storedName={}", deviceSubTypeID, newName);
        } else {
            log.warn("Отклонена загрузка пустого PDF: subtypeId={}, storedName={}", deviceSubTypeID, newName);
            throw new RuntimeException("Файл пустой " + newName);
        }
    }

    private long fileSize(MultipartFile file) {
        return file == null ? 0 : file.getSize();
    }

}
