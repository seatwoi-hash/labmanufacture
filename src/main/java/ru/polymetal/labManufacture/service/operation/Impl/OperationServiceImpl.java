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
import ru.polymetal.labManufacture.data.repository.OperationStatusRouteRepository;
import ru.polymetal.labManufacture.data.repository.RoleOperationStatusAccessRepository;
import ru.polymetal.labManufacture.dto.DeviceDto;
import ru.polymetal.labManufacture.service.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

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
    private final OperationStatusRepository deviceStatusRepository;
    private final RoleOperationStatusAccessRepository roleOperationStatusAccessRepository;
    private final OperationStatusRouteRepository operationStatusRouteRepository;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public Optional<Operation> findById(UUID operationId) {
        return operationRepository.findById(operationId);
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
        return performOperation(deviceId, account, targetStatus, description, false);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UUID completeRollbackOperation(UUID deviceId, Account account,
                                          String targetStatus, String description) {
        return performOperation(deviceId, account, targetStatus, description, true);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UUID completeOperationWithoutDescription(UUID deviceId, Account account,
                                                    String targetStatus) {
        return performOperation(deviceId, account, targetStatus, "", false);
    }

    private UUID performOperation(UUID operationId, Account account,
                                  String targetStatus, String description, boolean rollback) {

        Operation operation = operationRepository.findByIdWithLock(operationId)
                .orElseThrow(() -> new IllegalArgumentException("Операция по этому устройству не найдена"));

        OperationStatus newStatus = deviceStatusService.findByName(targetStatus);
        Operation newOperation = createNewDeviceVersion(
                operation, account, newStatus, description, rollback);
        markDeviceAsDeleted(operation);
        Operation savedOperation = operationRepository.save(newOperation);
        return savedOperation.getId();
    }

    @Override
    @Transactional
    public Operation createNewOperation(Device device, Account account,
                                        String description) {

        OperationStatus status = deviceStatusRepository.findByName(CREATE.getCode())
                .orElseThrow(() -> new IllegalArgumentException("Статус операции 'created' не найден"));

        Operation newOperation = new Operation();
        newOperation.setDevice(device);
        newOperation.setDescription(description);
        newOperation.setCreatedTime(LocalDateTime.now(clock));
        newOperation.setAccount(account);
        newOperation.setStatus(status);

        operationRepository.save(newOperation);

        return newOperation;
    }

    private void markDeviceAsDeleted(Operation operation) {
        operation.setDeletedAt(LocalDateTime.now(clock));
        operation.setIsDeleted(true);
    }

    @Override
    @Transactional(readOnly = true)
    public int getBoardsProducedToday() {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);

        return operationRepository.countByCreatedTimeBetweenAndDevice_Type_NameAndStatus_NameAndIsDeletedFalse(
                startOfDay, endOfDay, BOARD_TYPE_NAME, READY.getCode());
    }

    @Override
    public String getNextStatus(OperationStatus status) {
        if (status == null || status.getName() == null) {
            return "Неизвестный следующий статус";
        }
        return operationStatusRouteRepository.findFirstByCurrentStatus_NameOrderById(status.getName())
                .map(OperationStatusRoute::getNextOperationName)
                .orElse("Неизвестный следующий статус");
    }

    @Override
    @Transactional(readOnly = true)
    public String getPreviousStatus(OperationStatus status) {
        if (status == null || status.getName() == null) {
            return "Неизвестная предыдущая операция";
        }
        return operationStatusRouteRepository.findFirstByCurrentStatus_NameOrderById(status.getName())
                .map(OperationStatusRoute::getPreviousOperationName)
                .filter(name -> !name.isBlank())
                .orElse("Неизвестная предыдущая операция");
    }


    @Override
    @Transactional(readOnly = true)
    public List<Operation> findByStatusIdAndIsDelete(UUID statusId) {

        return operationRepository.findByStatusIdAndDeletedWithFetch(statusId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operation> findAll() {

        return operationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operation> findActiveOperationsForRollback() {
        return operationRepository.findActiveOperationsForRollback();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operation> findRollbackTargetsByDeviceIds(Collection<UUID> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Collections.emptyList();
        }
        return operationRepository.findRollbackTargetsByDeviceIds(deviceIds);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<Operation> findByStatusId(UUID statusId, Pageable pageable) {

        return operationRepository.findByStatusIdAndIsDeleted(statusId, false, pageable);
    }

    @Override
    @Transactional(readOnly = true)
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

    private Operation createNewDeviceVersion(Operation source, Account account,
                                             OperationStatus newStatus, String description,
                                             boolean rollback) {

        Operation newOperation = new Operation();
        Device device = source.getDevice();
        newOperation.setDevice(device);
        newOperation.setDescription(description);
        newOperation.setCreatedTime(LocalDateTime.now(clock));
        newOperation.setAccount(account);
        newOperation.setStatus(newStatus);
        newOperation.setIsRollback(rollback);

        return newOperation;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getNextStatusMapping() {
        return operationStatusRouteRepository.findAllWithCurrentStatus().stream()
                .collect(Collectors.toUnmodifiableMap(
                        route -> route.getCurrentStatus().getName(),
                        OperationStatusRoute::getNextOperationName,
                        (first, ignored) -> first));
    }

}
