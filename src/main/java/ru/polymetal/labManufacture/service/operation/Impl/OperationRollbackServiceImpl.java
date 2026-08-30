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

import java.util.UUID;

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

    private final OperationRepository operationRepository;
    private final OperationStatusRouteRepository operationStatusRouteRepository;
    private final OperationService operationService;
    private final SubtypeOperationRoutePolicy subtypeOperationRoutePolicy;

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

        return completeRollback(operationId, currentOperation, previousOperation, account, description, true);
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

        return completeRollback(operationId, currentOperation, targetOperation, account, description, false);
    }

    private UUID completeRollback(UUID operationId,
                                  Operation currentOperation,
                                  Operation targetOperation,
                                  Account account,
                                  String description,
                                  boolean requireAdjacentRoute) {

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
                operationId, account, previousStatus.getName(), rollbackDescription);
    }
}
