package ru.polymetal.labManufacture.service.operation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Контракт сервиса OperationService.
 *
 * @author Tatarinov Anton
 */
public interface OperationService {

    Optional<Operation> findById(UUID operationId);

    List<Operation> findDevicesForRole(Account account);

    UUID completeOperationWithDescription(UUID deviceId, Account account,
                                          String targetStatus, String description);

    UUID completeRollbackOperation(UUID deviceId, Account account,
                                   String targetStatus, String description,
                                   UUID rolledBackOperationId);

    UUID completeOperationWithoutDescription(UUID deviceId, Account account,
                                             String targetStatus);

    int getBoardsProducedToday();

    String getNextStatus(OperationStatus status);

    String getPreviousStatus(OperationStatus status);

    List<Operation> findByStatusIdAndIsDelete(UUID statusId);

    List<Operation> findAll();

    List<Operation> findActiveOperationsForRollback();

    List<Operation> findRollbackTargetsByDeviceIds(Collection<UUID> deviceIds);

    Page<Operation> findByStatusId(UUID statusId, Pageable pageable);

    Page<Operation> findByStatusIdAndSerialNumberContainingIgnoreCase(
            UUID statusId, String search, Pageable pageable);
    Operation createNewOperation(Device device, Account account,
                                        String description);
    List<Operation> findBySerialNumber(String sn);
    Map<String, String> getNextStatusMapping();
}
