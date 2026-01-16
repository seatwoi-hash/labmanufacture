package ru.polymetal.labManufacture.service.Impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.polymetal.labManufacture.data.models.*;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.data.repository.DeviceRepository;
import ru.polymetal.labManufacture.data.repository.DeviceStatusRepository;
import ru.polymetal.labManufacture.dto.DeviceDto;
import ru.polymetal.labManufacture.service.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private static final String BOARD_TYPE_NAME = "BOARD";

    private final DeviceRepository deviceRepository;
    private final AccountRepository accountRepository;
    private final DeviceStatusService deviceStatusService;
    private final DeviceTypeService deviceTypeService;
    private final DeviceSubTypeService deviceSubTypeService;
    private final DeviceStatusRepository deviceStatusRepository;

    private static final Map<String, List<String>> ROLE_STATUS_MAPPING = Map.of(
            "operator", Arrays.asList(CREATE, SIDE1),
            "quality", Arrays.asList(SIDE2, REPAIR1, REPAIR2, REPAIR3, INSTALLATION, WASHING1, VARNISH),
            "output", Arrays.asList(QUALITY_CHECK_1),
            "repairman", Arrays.asList(FAIL_QUALITY_CHECK_1, FAIL_QUALITY_CHECK_2,
                    FAIL_QUALITY_CHECK_3, FAIL_QUALITY_CHECK_4, FAIL_TEST),
            "washer", Arrays.asList(QUALITY_CHECK_2, TEST),
            "varnisher", Arrays.asList(WASHING2),
            "testerb", Arrays.asList(QUALITY_CHECK_4, QUALITY_CHECK_3),
            "user", Arrays.asList(CREATE)
    );

    private static final Map<String, String> NEXT_STATUS_MAPPING = Map.ofEntries(
            Map.entry(CREATE, "Монтаж \"Сторона 1\""),
            Map.entry(SIDE1, "Монтаж \"Сторона 2\""),
            Map.entry(SIDE2, "ОТК №1"),
            Map.entry(QUALITY_CHECK_1, "Выводного монтажа"),
            Map.entry(FAIL_QUALITY_CHECK_1, "Ремонт №1"),
            Map.entry(REPAIR1, "ОТК №1"),
            Map.entry(INSTALLATION, "ОТК №2"),
            Map.entry(QUALITY_CHECK_2, "Мойка №1"),
            Map.entry(FAIL_QUALITY_CHECK_2, "Ремонт №2"),
            Map.entry(REPAIR2, "ОТК №2"),
            Map.entry(WASHING1, "ОТК №3"),
            Map.entry(QUALITY_CHECK_3, "Тестировка"),
            Map.entry(FAIL_QUALITY_CHECK_3, "Мойка №1"),
            Map.entry(TEST, "Мойка №2"),
            Map.entry(FAIL_TEST, "Ремонт №3"),
            Map.entry(REPAIR3, "ОТК №4"),
            Map.entry(FAIL_QUALITY_CHECK_4, "Ремонт №3"),
            Map.entry(QUALITY_CHECK_4, "Тестировка"),
            Map.entry(WASHING2, "Нанесение компаунда"),
            Map.entry(VARNISH, "ОТК №5"),
            Map.entry(QUALITY_CHECK_5, "Готовые платы"),
            Map.entry(FAIL_QUALITY_CHECK_5, "Нанесение компаунда")
    );

    @Override
    @Transactional
    public void createDevice(DeviceDto deviceDto, String username) {
        log.info("Создание устройства для пользователя: {}", username);

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        validateDeviceDto(deviceDto);

        DeviceSubType subtype = deviceSubTypeService.findById(deviceDto.getSubType().getId())
                .orElseThrow(() -> new IllegalArgumentException("Подтип не найден"));

        if (deviceRepository.existsBySerialNumber(deviceDto.getSerialNumber())) {
            throw new IllegalArgumentException(
                    String.format("Устройство с серийным номером '%s' уже существует", deviceDto.getSerialNumber()));
        }

        Device device = buildDevice(deviceDto, account, subtype);
        Device savedDevice = deviceRepository.save(device);

        log.info("Устройство успешно создано с ID: {}", savedDevice.getId());
    }

    @Override
    public void validateDeviceDto(DeviceDto deviceDto) {
        if (deviceDto.getSerialNumber() == null || deviceDto.getSerialNumber().isBlank()) {
            throw new IllegalArgumentException("Серийный номер не может быть пустым");
        }
    }

    @Override
    public Device buildDevice(DeviceDto deviceDto, Account account, DeviceSubType subtype) {
        Device device = new Device();
        device.setSerialNumber(deviceDto.getSerialNumber());
        device.setAccount(account);
        device.setStatus(deviceStatusService.findByName(CREATE));
        device.setType(deviceTypeService.findByName(BOARD_TYPE_NAME));
        device.setSubType(subtype);
        return device;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Device> findDevicesForRole(Account account) {
        Set<String> statusNames = collectStatusNamesForRoles(account.getRoles());

        if (statusNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> statusIds = getStatusIdsByNames(statusNames);

        if (statusIds.isEmpty()) {
            return Collections.emptyList();
        }

        return deviceRepository.findByStatusIdInAndIsDeletedWithFetch(statusIds, false);
    }

    @Override
    public Set<String> collectStatusNamesForRoles(Set<Role> roles) {
        Set<String> statusNames = new HashSet<>();

        for (Role role : roles) {
            String roleName = role.getName().toLowerCase();

            if ("admin".equals(roleName)) {
                return new HashSet<>(getAllStatusNames());
            }

            List<String> roleStatuses = ROLE_STATUS_MAPPING.get(roleName);
            if (roleStatuses != null) {
                statusNames.addAll(roleStatuses);
            }
        }

        return statusNames;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllStatusNames() {
        return deviceStatusRepository.findAll().stream()
                .map(DeviceStatus::getName)
                .filter(name -> !READY.equals(name))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getStatusIdsByNames(Collection<String> statusNames) {
        return deviceStatusService.findByListName(new ArrayList<>(statusNames)).stream()
                .map(DeviceStatus::getId)
                .toList();
    }

    @Override
    @Transactional
    public void completeOperationWithDescription(UUID deviceId, Account account,
                                                 String targetStatus, String description) {
        performOperation(deviceId, account, targetStatus, description);
    }

    @Override
    @Transactional
    public void completeOperationWithoutDescription(UUID deviceId, Account account,
                                                    String targetStatus) {
        performOperation(deviceId, account, targetStatus, "");
    }

    @Override
    @Transactional
    public void performOperation(UUID deviceId, Account account,
                                 String targetStatus, String description) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Устройство не найдено"));

        DeviceStatus newStatus = deviceStatusService.findByName(targetStatus);
        Device newDevice = createNewDeviceVersion(device, account, newStatus, description);

        markDeviceAsDeleted(device);
        deviceRepository.save(newDevice);
    }

    @Override
    public Device createNewDeviceVersion(Device source, Account account,
                                         DeviceStatus newStatus, String description) {
        Device newDevice = new Device();
        newDevice.setSerialNumber(source.getSerialNumber());
        newDevice.setDescription(description);
        newDevice.setType(source.getType());
        newDevice.setSubType(source.getSubType());
        newDevice.setCreatedTime(source.getCreatedTime());
        newDevice.setAccount(account);
        newDevice.setStatus(newStatus);
        return newDevice;
    }

    @Override
    @Transactional
    public void markDeviceAsDeleted(Device device) {
        device.setDeletedAt(LocalDateTime.now());
        device.setIsDeleted(true);
        deviceRepository.save(device);
    }

    @Override
    @Transactional(readOnly = true)
    public int getBoardsProducedToday() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);

        return deviceRepository.countByCreatedTimeBetweenAndTypeNameAndStatusNameAndIsDeletedFalse(
                startOfDay, endOfDay, BOARD_TYPE_NAME, READY);
    }

    @Override
    public String getNextStatus(DeviceStatus status) {
        return NEXT_STATUS_MAPPING.getOrDefault(status.getName(), "Неизвестный следующий статус");
    }


    @Override
    @Transactional
    public List<Device> findByStatusIdAndIsDelete(UUID statusId) {
        return deviceRepository.findByStatusIdAndDeletedWithFetch(statusId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Device> findAll() {
        return deviceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsSerialNumber(String sn) {
        return deviceRepository.existsBySerialNumber(sn);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Device> findBySerialNumber(String sn) {
        return deviceRepository.findBySerialNumber(sn);
    }

    @Override
    public Page<Device> findByStatusId(UUID statusId, Pageable pageable) {
        return deviceRepository.findByStatusIdAndIsDeleted(statusId, false, pageable);    }

    @Override
    public Page<Device> findByStatusIdAndSerialNumberContainingIgnoreCase(UUID statusId, String search, Pageable pageable) {
        return deviceRepository.findByStatusIdAndSerialNumberContainingIgnoreCase(
                statusId, search, false, pageable);
    }

}
