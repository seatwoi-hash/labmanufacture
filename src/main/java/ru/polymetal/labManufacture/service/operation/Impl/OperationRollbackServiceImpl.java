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

        OperationStatus currentStatus = currentOperation.getStatus();
        OperationStatus previousStatus = previousOperation.getStatus();
        if (currentStatus == null || currentStatus.getId() == null
                || previousStatus == null || previousStatus.getId() == null) {
            throw new OperationRollbackException("Статус операции не определён");
        }

        if (!operationStatusRouteRepository.areAdjacent(currentStatus.getId(), previousStatus.getId())) {
            throw new OperationRollbackException(
                    "Возврат из статуса '%s' в статус '%s' отсутствует в маршруте"
                            .formatted(currentStatus.getName(), previousStatus.getName()));
        }

        String rollbackDescription = ROLLBACK_DESCRIPTION_PREFIX
                + (description == null ? "" : description.trim());
        log.info("Rolling back operation: operationId={}, fromStatus={}, toStatus={}, account={}",
                operationId, currentStatus.getName(), previousStatus.getName(), account.getUsername());

        return operationService.completeOperationWithDescription(
                operationId, account, previousStatus.getName(), rollbackDescription);
    }
}
