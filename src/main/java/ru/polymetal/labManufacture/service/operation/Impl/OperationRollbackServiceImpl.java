package ru.polymetal.labManufacture.service.operation.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.repository.OperationRepository;
import ru.polymetal.labManufacture.data.repository.OperationStatusRouteRepository;
import ru.polymetal.labManufacture.exception.OperationNotFoundException;
import ru.polymetal.labManufacture.exception.OperationRollbackException;
import ru.polymetal.labManufacture.service.operation.OperationRollbackService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import ru.polymetal.labManufacture.service.operation.SubtypeOperationRoutePolicy;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL3;

/**
 * Возвращает операцию к последнему фактическому статусу с проверкой маршрута.
 *
 * @author Tatarinov Anton
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OperationRollbackServiceImpl implements OperationRollbackService {

    private static final String ROLLBACK_DESCRIPTION_PREFIX = "Возвращён - ";
    private static final Duration CANCELLATION_WINDOW = Duration.ofMinutes(15);

    private final OperationRepository operationRepository;
    private final OperationStatusRouteRepository operationStatusRouteRepository;
    private final OperationService operationService;
    private final SubtypeOperationRoutePolicy subtypeOperationRoutePolicy;
    private final Clock clock;

    @Override
    @Transactional
    public UUID rollback(UUID operationId, Account account, String description) {
        Operation currentOperation = operationService.findById(operationId)
                .filter(operation -> !Boolean.TRUE.equals(operation.getIsDeleted()))
                .orElseThrow(OperationNotFoundException::new);

        if (currentOperation.getDevice() == null || currentOperation.getDevice().getId() == null) {
            throw new OperationRollbackException("У операции отсутствует устройство");
        }

        Operation previousOperation = operationRepository
                .findFirstByDevice_IdAndIsDeletedTrueOrderByDeletedAtDesc(currentOperation.getDevice().getId())
                .orElseThrow(() -> new OperationRollbackException("Предыдущая операция не найдена"));

        return completeRollback(
                operationId, currentOperation, previousOperation, account, description, true, operationId);
    }

    @Override
    @Transactional
    public UUID rollbackTo(UUID operationId, UUID targetOperationId, Account account, String description) {
        Operation currentOperation = operationService.findById(operationId)
                .filter(operation -> !Boolean.TRUE.equals(operation.getIsDeleted()))
                .orElseThrow(OperationNotFoundException::new);

        if (currentOperation.getDevice() == null || currentOperation.getDevice().getId() == null) {
            throw new OperationRollbackException("У операции отсутствует устройство");
        }

        Operation targetOperation = operationService.findById(targetOperationId)
                .filter(operation -> Boolean.TRUE.equals(operation.getIsDeleted()))
                .filter(operation -> operation.getDevice() != null)
                .filter(operation -> currentOperation.getDevice().getId().equals(operation.getDevice().getId()))
                .filter(operation -> operation.getCreatedTime() != null
                        && currentOperation.getCreatedTime() != null
                        && operation.getCreatedTime().isBefore(currentOperation.getCreatedTime()))
                .orElseThrow(() -> new OperationRollbackException(
                        "Выбранный этап отсутствует в предыдущей истории платы"));

        String targetStageName = operationStatusRouteRepository
                .findFirstByCurrentStatus_NameOrderById(targetOperation.getStatus().getName())
                .map(route -> route.getNextOperationName())
                .orElseThrow(() -> new OperationRollbackException(
                        "Для выбранного этапа отсутствует производственный маршрут"));
        if (!subtypeOperationRoutePolicy.isAllowed(
                currentOperation.getDevice().getSubtype(), targetStageName)) {
            throw new OperationRollbackException(
                    "Этап '%s' отключён для данного типа платы".formatted(targetStageName));
        }

        return completeRollback(
                operationId, currentOperation, targetOperation, account, description, false, operationId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCancelOwnLastOperation(UUID operationId, Account account) {
        try {
            validateCancellation(operationId, account, false);
            return true;
        } catch (OperationNotFoundException | OperationRollbackException exception) {
            return false;
        }
    }

    @Override
    @Transactional
    public UUID cancelOwnLastOperation(UUID operationId, Account account, String comment) {
        String normalizedComment = comment == null ? "" : comment.trim();
        if (normalizedComment.isEmpty()) {
            throw new OperationRollbackException("Укажите комментарий возврата");
        }
        CancellationContext context = validateCancellation(operationId, account, true);
        log.info("Cancelling own last operation: requestedOperationId={}, activeOperationId={}, account={}",
                operationId, context.activeOperation().getId(), account.getUsername());
        return completeRollback(
                context.activeOperation().getId(),
                context.activeOperation(),
                context.previousOperation(),
                account,
                normalizedComment,
                false,
                operationId);
    }

    private CancellationContext validateCancellation(
            UUID operationId, Account account, boolean lockActiveOperation) {
        if (account == null || isAdmin(account)) {
            throw new OperationRollbackException("Администратору недоступна отмена своей операции");
        }

        Operation requestedOperation = operationService.findById(operationId)
                .orElseThrow(OperationNotFoundException::new);
        if (Boolean.TRUE.equals(requestedOperation.getIsRollback())) {
            throw new OperationRollbackException("Операцию возврата нельзя отменить повторно");
        }
        if (requestedOperation.getDevice() == null || requestedOperation.getDevice().getId() == null) {
            throw new OperationRollbackException("У операции отсутствует устройство");
        }

        UUID deviceId = requestedOperation.getDevice().getId();
        Operation activeOperation = lockActiveOperation
                ? operationRepository.findActiveByDeviceIdWithLock(deviceId)
                    .orElseThrow(() -> new OperationRollbackException("Активная операция платы не найдена"))
                : operationRepository.findHistoryByDeviceId(deviceId).stream()
                    .filter(operation -> !Boolean.TRUE.equals(operation.getIsDeleted()))
                    .findFirst()
                    .orElseThrow(() -> new OperationRollbackException("Активная операция платы не найдена"));
        List<Operation> history = operationRepository.findHistoryByDeviceId(deviceId);

        Operation lastVisibleOperation = history.stream()
                .filter(this::isVisibleInOperationHistory)
                .findFirst()
                .orElseThrow(() -> new OperationRollbackException("История операций платы пуста"));
        if (!operationId.equals(lastVisibleOperation.getId())) {
            throw new OperationRollbackException("Отменить можно только последнюю операцию платы");
        }
        if (requestedOperation.getAccount() == null
                || !Objects.equals(requestedOperation.getAccount().getId(), account.getId())) {
            throw new OperationRollbackException("Отменить операцию может только её автор");
        }
        if (requestedOperation.getCreatedTime() == null
                || requestedOperation.getCreatedTime().isBefore(
                        LocalDateTime.now(clock).minus(CANCELLATION_WINDOW))) {
            throw new OperationRollbackException("С момента создания операции прошло более 15 минут");
        }

        int requestedIndex = indexOf(history, operationId);
        if (requestedIndex < 0 || requestedIndex + 1 >= history.size()) {
            throw new OperationRollbackException("Предыдущая операция не найдена");
        }
        Operation previousOperation = history.get(requestedIndex + 1);
        return new CancellationContext(activeOperation, previousOperation);
    }

    private int indexOf(List<Operation> history, UUID operationId) {
        for (int index = 0; index < history.size(); index++) {
            if (operationId.equals(history.get(index).getId())) {
                return index;
            }
        }
        return -1;
    }

    private boolean isVisibleInOperationHistory(Operation operation) {
        return Boolean.TRUE.equals(operation.getIsRollback()) || !isTechnical(operation);
    }

    private boolean isTechnical(Operation operation) {
        if (operation.getStatus() == null || operation.getStatus().getName() == null) {
            return false;
        }
        String statusName = operation.getStatus().getName();
        return TECHNICAL.matches(statusName)
                || TECHNICAL2.matches(statusName)
                || TECHNICAL3.matches(statusName);
    }

    private boolean isAdmin(Account account) {
        return account.getRoles() != null && account.getRoles().stream()
                .anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
    }

    private record CancellationContext(
            Operation activeOperation,
            Operation previousOperation) {
    }

    private UUID completeRollback(UUID operationId,
                                  Operation currentOperation,
                                  Operation targetOperation,
                                  Account account,
                                  String description,
                                  boolean requireAdjacentRoute,
                                  UUID rolledBackOperationId) {

        OperationStatus currentStatus = currentOperation.getStatus();
        OperationStatus previousStatus = targetOperation.getStatus();
        if (currentStatus == null || currentStatus.getId() == null
                || previousStatus == null || previousStatus.getId() == null) {
            throw new OperationRollbackException("Статус операции не определён");
        }

        if (requireAdjacentRoute
                && !operationStatusRouteRepository.areAdjacent(currentStatus.getId(), previousStatus.getId())) {
            throw new OperationRollbackException(
                    "Возврат из статуса '%s' в статус '%s' отсутствует в маршруте"
                            .formatted(currentStatus.getName(), previousStatus.getName()));
        }

        String rollbackDescription = ROLLBACK_DESCRIPTION_PREFIX
                + (description == null ? "" : description.trim());
        log.info("Rolling back operation: operationId={}, fromStatus={}, toStatus={}, account={}",
                operationId, currentStatus.getName(), previousStatus.getName(), account.getUsername());

        return operationService.completeRollbackOperation(
                operationId, account, previousStatus.getName(), rollbackDescription,
                rolledBackOperationId);
    }
}
