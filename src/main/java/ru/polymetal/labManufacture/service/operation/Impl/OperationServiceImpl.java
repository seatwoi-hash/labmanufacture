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
import ru.polymetal.labManufacture.data.repository.RoleOperationStatusAccessRepository;
import ru.polymetal.labManufacture.dto.DeviceDto;
import ru.polymetal.labManufacture.service.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.*;
import ru.polymetal.labManufacture.service.operation.OperationService;

/**
 * Реализация сервиса OperationServiceImpl.
 *
 * @author Tatarinov Anton
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class  OperationServiceImpl implements OperationService {

    private static final String BOARD_TYPE_NAME = "BOARD";

    private final OperationRepository operationRepository;
    private final DeviceStatusService deviceStatusService;
    private final DeviceTypeService deviceTypeService;
    private final OperationStatusRepository deviceStatusRepository;
    private final RoleOperationStatusAccessRepository roleOperationStatusAccessRepository;

    private static final Map<String, String> NEXT_STATUS_MAPPING = Map.ofEntries(
            Map.entry(CREATE.getCode(), "Монтаж \"Сторона 1\""),
            Map.entry(SIDE1.getCode(), "Монтаж \"Сторона 2\""),
            Map.entry(SIDE2.getCode(), "ОТК №1"),
            Map.entry(QUALITY_CHECK_1.getCode(), "Выводной монтаж №1"),
            Map.entry(FAIL_QUALITY_CHECK_1.getCode(), "Ремонт №1"),
            Map.entry(REPAIR1.getCode(), "ОТК №1.1"),
            Map.entry(QUALITY_CHECK_1_1.getCode(), "Выводной монтаж №1"),
            Map.entry(FAIL_QUALITY_CHECK_1_1.getCode(), "Ремонт №1"),
            Map.entry(INSTALLATION.getCode(), "ОТК №2"),
            Map.entry(QUALITY_CHECK_2.getCode(), "Тестировка"),
            Map.entry(FAIL_QUALITY_CHECK_2.getCode(), "Ремонт №2"),
            Map.entry(QUALITY_CHECK_2_1.getCode(), "Тестировка"),
            Map.entry(FAIL_QUALITY_CHECK_2_1.getCode(), "Ремонт №2"),
            Map.entry(REPAIR2.getCode(), "ОТК №2.1"),
            Map.entry(TEST.getCode(), "Выводной монтаж №2"),
            Map.entry(FAIL_TEST.getCode(), "Ремонт №3"),
            Map.entry(QUALITY_CHECK_3.getCode(), "Тестировка"),
            Map.entry(FAIL_QUALITY_CHECK_3.getCode(), "Ремонт №3"),

            Map.entry(INSTALLATION2.getCode(), "ОТК №4"),
            Map.entry(QUALITY_CHECK_4.getCode(), "Тестировка №2"),
            Map.entry(FAIL_QUALITY_CHECK_4.getCode(), "Ремонт №4"),
            Map.entry(QUALITY_CHECK_4_1.getCode(), "Тестировка №2"),
            Map.entry(FAIL_QUALITY_CHECK_4_1.getCode(), "Ремонт №4"),
            Map.entry(REPAIR4.getCode(), "ОТК №4.1"),
            Map.entry(DIAGNOSTICIAN_REPAIR_1.getCode(), "Ремонт №3"),
            Map.entry(DIAGNOSTICIAN_REPAIR_2.getCode(), "Ремонт №5"),
            Map.entry(DIAGNOSTICIAN_TEST_1.getCode(), "Тестировка"),
            Map.entry(DIAGNOSTICIAN_TEST_2.getCode(), "Тестировка №2"),


            Map.entry(TEST_2.getCode(), "Отмывка №1"),
            Map.entry(FAIL_TEST_2.getCode(), "Ремонт №5"),
            Map.entry(QUALITY_CHECK_4_2.getCode(), "Тестировка №2"),
            Map.entry(FAIL_QUALITY_CHECK_4_2.getCode(), "Ремонт №5"),
            Map.entry(REPAIR5.getCode(), "ОТК №4.2"),
            Map.entry(QUALITY_CHECK_5_1.getCode(), "Отмывка №2"),
            Map.entry(FAIL_QUALITY_CHECK_5_1.getCode(), "Ремонт №6"),


            Map.entry(WASHING1.getCode(), "ОТК №5"),
            Map.entry(REPAIR3.getCode(), "ОТК №3"),

            Map.entry(QUALITY_CHECK_5.getCode(), "Нанесение компаунда"),
            Map.entry(QUALITY_CHECK_5_1_1.getCode(), "Нанесение компаунда"),
            Map.entry(FAIL_QUALITY_CHECK_5.getCode(), "Ремонт №6"),
            Map.entry(FAIL_QUALITY_CHECK_5_1_1.getCode(), "Отмывка №2"),
            Map.entry(WASHING2.getCode(), "ОТК №5"),
            Map.entry(REPAIR6.getCode(), "ОТК №5.1"),
            Map.entry(VARNISH.getCode(), "ОТК №6"),
            Map.entry(QUALITY_CHECK_6.getCode(), "Готовые платы"),
            Map.entry(FAIL_QUALITY_CHECK_6.getCode(), "Нанесение компаунда"),
            Map.entry(TECHNICAL.getCode(), "Тестировка №1"),
            Map.entry(TECHNICAL2.getCode(), "Отмывка №1"),
            Map.entry(TECHNICAL3.getCode(), "ОТК №1")

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
        operation.setStatus(deviceStatusService.findByName(CREATE.getCode()));

        return operation;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operation> findDevicesForRole(Account account) {
        Set<Role> roles = Optional.ofNullable(account.getRoles()).orElseGet(Collections::emptySet);
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }

        boolean isAdmin = roles.stream()
                .map(Role::getName)
                .filter(Objects::nonNull)
                .anyMatch("admin"::equalsIgnoreCase);

        List<UUID> statusIds;
        if (isAdmin) {
            statusIds = deviceStatusRepository.findIdsByNameNot(READY.getCode());
        } else {
            List<UUID> roleIds = roles.stream()
                    .map(Role::getId)
                    .filter(Objects::nonNull)
                    .toList();

            if (roleIds.isEmpty()) {
                return Collections.emptyList();
            }
            statusIds = roleOperationStatusAccessRepository.findOperationStatusIdsByRoleIds(roleIds);
        }

        if (statusIds.isEmpty()) {
            return Collections.emptyList();
        }

        return operationRepository.findByStatusIdInAndIsDeletedWithFetch(statusIds, false);
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

        OperationStatus status = deviceStatusRepository.findByName(CREATE.getCode())
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
                startOfDay, endOfDay, BOARD_TYPE_NAME, READY.getCode());
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
