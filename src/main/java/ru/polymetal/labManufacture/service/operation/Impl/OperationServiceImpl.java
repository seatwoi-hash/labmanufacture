package ru.polymetal.labManufacture.service.operation.Impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.polymetal.labManufacture.data.models.*;
import ru.polymetal.labManufacture.data.repository.OperationRepository;
import ru.polymetal.labManufacture.data.repository.OperationStatusRepository;
import ru.polymetal.labManufacture.dto.DeviceDto;
import ru.polymetal.labManufacture.service.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.*;
import ru.polymetal.labManufacture.service.operation.OperationService;

@Service
@Slf4j
@RequiredArgsConstructor
public class  OperationServiceImpl implements OperationService {

    private static final String BOARD_TYPE_NAME = "BOARD";

    private final OperationRepository operationRepository;
    private final DeviceStatusService deviceStatusService;
    private final DeviceTypeService deviceTypeService;
    private final OperationStatusRepository deviceStatusRepository;

    private static final Map<String, List<String>> ROLE_STATUS_MAPPING = Map.of(
            "operator", Arrays.asList(CREATE, SIDE1),
            "diagnostician", Arrays.asList(FAIL_TEST, FAIL_TEST_2),

            "quality", Arrays.asList(SIDE2, REPAIR1, REPAIR2, REPAIR3, REPAIR4, REPAIR5, INSTALLATION, INSTALLATION2,
                    REPAIR6,
                    WASHING1,
                    WASHING2,
                    VARNISH),
            "output", Arrays.asList(QUALITY_CHECK_1, TEST, QUALITY_CHECK_1_1),
            "repairman", Arrays.asList(FAIL_QUALITY_CHECK_1,FAIL_QUALITY_CHECK_1_1, FAIL_QUALITY_CHECK_2,
                    FAIL_QUALITY_CHECK_2_1, FAIL_QUALITY_CHECK_3, FAIL_QUALITY_CHECK_4, FAIL_QUALITY_CHECK_4_1,
                    FAIL_TEST, FAIL_TEST_2, FAIL_QUALITY_CHECK_4_2, FAIL_QUALITY_CHECK_5, FAIL_QUALITY_CHECK_5_1),
            "washer", Arrays.asList(TEST_2, QUALITY_CHECK_5_1, TECHNICAL2, FAIL_QUALITY_CHECK_5_1_1),
            "varnisher", Arrays.asList(QUALITY_CHECK_5, FAIL_QUALITY_CHECK_6, QUALITY_CHECK_5_1_1),
            "testerb", Arrays.asList(QUALITY_CHECK_2, QUALITY_CHECK_2_1, QUALITY_CHECK_4, QUALITY_CHECK_3, TECHNICAL, QUALITY_CHECK_4_1, QUALITY_CHECK_4_2),
            "user", Arrays.asList(CREATE)
    );

    private static final Map<String, String> NEXT_STATUS_MAPPING = Map.ofEntries(
            Map.entry(CREATE, "Монтаж \"Сторона 1\""),
            Map.entry(SIDE1, "Монтаж \"Сторона 2\""),
            Map.entry(SIDE2, "ОТК №1"),
            Map.entry(QUALITY_CHECK_1, "Выводной монтаж №1"),
            Map.entry(FAIL_QUALITY_CHECK_1, "Ремонт №1"),
            Map.entry(REPAIR1, "ОТК №1.1"),
            Map.entry(QUALITY_CHECK_1_1, "Выводной монтаж №1"),
            Map.entry(FAIL_QUALITY_CHECK_1_1, "Ремонт №1"),
            Map.entry(INSTALLATION, "ОТК №2"),
            Map.entry(QUALITY_CHECK_2, "Тестировка"),
            Map.entry(FAIL_QUALITY_CHECK_2, "Ремонт №2"),
            Map.entry(QUALITY_CHECK_2_1, "Тестировка"),
            Map.entry(FAIL_QUALITY_CHECK_2_1, "Ремонт №2"),
            Map.entry(REPAIR2, "ОТК №2.1"),
            Map.entry(TEST, "Выводной монтаж №2"),
            Map.entry(FAIL_TEST, "Ремонт №3"),
            Map.entry(QUALITY_CHECK_3, "Тестировка"),
            Map.entry(FAIL_QUALITY_CHECK_3, "Ремонт №3"),

            Map.entry(INSTALLATION2, "ОТК №4"),
            Map.entry(QUALITY_CHECK_4, "Тестировка №2"),
            Map.entry(FAIL_QUALITY_CHECK_4, "Ремонт №4"),
            Map.entry(QUALITY_CHECK_4_1, "Тестировка №2"),
            Map.entry(FAIL_QUALITY_CHECK_4_1, "Ремонт №4"),
            Map.entry(REPAIR4, "ОТК №4.1"),
            Map.entry(DIAGNOSTICIAN_REPAIR_1, "Ремонт №3"),
            Map.entry(DIAGNOSTICIAN_REPAIR_2, "Ремонт №5"),
            Map.entry(DIAGNOSTICIAN_TEST_1, "Тестировка"),
            Map.entry(DIAGNOSTICIAN_TEST_2, "Тестировка №2"),


            Map.entry(TEST_2, "Отмывка №1"),
            Map.entry(FAIL_TEST_2, "Ремонт №5"),
            Map.entry(QUALITY_CHECK_4_2, "Тестировка №2"),
            Map.entry(FAIL_QUALITY_CHECK_4_2, "Ремонт №5"),
            Map.entry(REPAIR5, "ОТК №4.2"),
            Map.entry(QUALITY_CHECK_5_1, "Отмывка №2"),
            Map.entry(FAIL_QUALITY_CHECK_5_1, "Ремонт №6"),


            Map.entry(WASHING1, "ОТК №5"),
            Map.entry(REPAIR3, "ОТК №3"),

            Map.entry(QUALITY_CHECK_5, "Нанесение компаунда"),
            Map.entry(QUALITY_CHECK_5_1_1, "Нанесение компаунда"),
            Map.entry(FAIL_QUALITY_CHECK_5, "Ремонт №6"),
            Map.entry(FAIL_QUALITY_CHECK_5_1_1, "Отмывка №2"),
            Map.entry(WASHING2, "ОТК №5"),
            Map.entry(REPAIR6, "ОТК №5.1"),
            Map.entry(VARNISH, "ОТК №6"),
            Map.entry(QUALITY_CHECK_6, "Готовые платы"),
            Map.entry(FAIL_QUALITY_CHECK_6, "Нанесение компаунда"),
            Map.entry(TECHNICAL, "Тестировка №1"),
            Map.entry(TECHNICAL2, "Отмывка №1"),
            Map.entry(TECHNICAL3, "ОТК №1")

    );

    @Override
    public Optional<Operation> findById(UUID operationId) {
        return operationRepository.findById(operationId);
    }

    @Override
    public void validateDeviceDto(DeviceDto deviceDto) {
        if (deviceDto.getSerialNumber() == null || deviceDto.getSerialNumber().isBlank()) {
            throw new IllegalArgumentException("Серийный номер не может быть пустым");
        }
    }

    @Override
    @Transactional
    public Operation buildDevice(DeviceDto deviceDto, Account account, DeviceSubType subtype) {

        Operation operation = new Operation();
        Device device = new Device();

        device.setSubtype(subtype);
        device.setType(deviceTypeService.findByName(BOARD_TYPE_NAME));
        device.setSerialNumber(deviceDto.getSerialNumber());

        operation.setDevice(device);
        operation.setAccount(account);
        operation.setStatus(deviceStatusService.findByName(CREATE));

        return operation;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operation> findDevicesForRole(Account account) {
        Set<String> statusNames = collectStatusNamesForRoles(account.getRoles());

        if (statusNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> statusIds = getStatusIdsByNames(statusNames);

        if (statusIds.isEmpty()) {
            return Collections.emptyList();
        }

        return operationRepository.findByStatusIdInAndIsDeletedWithFetch(statusIds, false);
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
                .map(OperationStatus::getName)
                .filter(name -> !READY.equals(name))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getStatusIdsByNames(Collection<String> statusNames) {
        return deviceStatusService.findByListName(new ArrayList<>(statusNames)).stream()
                .map(OperationStatus::getId)
                .toList();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UUID completeOperationWithDescription(UUID deviceId, Account account,
                                                 String targetStatus, String description) {
        return performOperation(deviceId, account, targetStatus, description);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UUID completeOperationWithoutDescription(UUID deviceId, Account account,
                                                    String targetStatus) {
        return performOperation(deviceId, account, targetStatus, "");
    }

    @Override
    @Transactional
    public UUID performOperation(UUID operationId, Account account,
                                 String targetStatus, String description) {

        Operation operation = operationRepository.findByIdWithLock(operationId)
                .orElseThrow(() -> new IllegalArgumentException("Операция по этому устройству не найдена"));

        OperationStatus newStatus = deviceStatusService.findByName(targetStatus);
        Operation newOperation = createNewDeviceVersion(operation, account, newStatus, description);
        markDeviceAsDeleted(operation);
        operationRepository.save(newOperation);

        return operationRepository.save(newOperation).getId();
    }

    @Override
    @Transactional
    public Operation createNewOperation(Device device, Account account,
                                        String description) {

        OperationStatus status = deviceStatusRepository.findByName(CREATE)
                .orElseThrow(() -> new IllegalArgumentException("Подтип не найден"));

        Operation newOperation = new Operation();
        newOperation.setDevice(device);
        newOperation.setDescription(description);
        newOperation.setCreatedTime(LocalDateTime.now());
        newOperation.setAccount(account);
        newOperation.setStatus(status);

        operationRepository.save(newOperation);

        return newOperation;
    }

    @Override
    @Transactional
    public void markDeviceAsDeleted(Operation operation) {
        operation.setDeletedAt(LocalDateTime.now());
        operation.setIsDeleted(true);
        operationRepository.save(operation);
    }

    @Override
    @Transactional(readOnly = true)
    public int getBoardsProducedToday() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);

        return operationRepository.countByCreatedTimeBetweenAndDevice_Type_NameAndStatus_NameAndIsDeletedFalse(
                startOfDay, endOfDay, BOARD_TYPE_NAME, READY);
    }

    @Override
    public String getNextStatus(OperationStatus status) {

        return NEXT_STATUS_MAPPING.getOrDefault(status.getName(), "Неизвестный следующий статус");
    }


    @Override
    @Transactional
    public List<Operation> findByStatusIdAndIsDelete(UUID statusId) {

        return operationRepository.findByStatusIdAndDeletedWithFetch(statusId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operation> findAll() {

        return operationRepository.findAll();
    }


    @Override
    public Page<Operation> findByStatusId(UUID statusId, Pageable pageable) {

        return operationRepository.findByStatusIdAndIsDeleted(statusId, false, pageable);
    }

    @Override
    public Page<Operation> findByStatusIdAndSerialNumberContainingIgnoreCase(UUID statusId, String search,
                                                                             Pageable pageable) {
        return operationRepository.findByStatusIdAndSerialNumberContainingIgnoreCase(
                statusId, search, false, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operation> findBySerialNumber(String sn) {

        return operationRepository.findByDevice_SerialNumber(sn);
    }

    @Override
    @Transactional
    public Operation createNewDeviceVersion(Operation source, Account account,
                                            OperationStatus newStatus, String description) {

        Operation newOperation = new Operation();
        Device device = source.getDevice();
        newOperation.setDevice(device);
        newOperation.setDescription(description);
        newOperation.setCreatedTime(source.getCreatedTime());
        newOperation.setAccount(account);
        newOperation.setStatus(newStatus);

        return newOperation;
    }

    public Map<String, String> getNEXT_STATUS_MAPPING() {

        return NEXT_STATUS_MAPPING;
    }

}
