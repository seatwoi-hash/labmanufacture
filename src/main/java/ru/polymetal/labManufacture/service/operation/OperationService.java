package ru.polymetal.labManufacture.service.operation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.dto.DeviceDto;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Контракт сервиса OperationService.
 *
 * @author Tatarinov Anton
 */
public interface OperationService {

    Optional<Operation> findById(UUID operationId);

    void validateDeviceDto(DeviceDto deviceDto);

    Operation buildDevice(DeviceDto deviceDto, Account account, DeviceSubType subtype);

    List<Operation> findDevicesForRole(Account account);

    Set<String> collectStatusNamesForRoles(Set<Role> roles);

    List<String> getAllStatusNames();

    List<UUID> getStatusIdsByNames(Collection<String> statusNames);


    UUID completeOperationWithDescription(UUID deviceId, Account account,
                                          String targetStatus, String description);

    UUID completeOperationWithoutDescription(UUID deviceId, Account account,
                                             String targetStatus);

    UUID performOperation(UUID deviceId, Account account,
                          String targetStatus, String description);

    void markDeviceAsDeleted(Operation device);

    int getBoardsProducedToday();

    String getNextStatus(OperationStatus status);

    List<Operation> findByStatusIdAndIsDelete(UUID statusId);

    List<Operation> findAll();

    Page<Operation> findByStatusId(UUID statusId, Pageable pageable);

    Page<Operation> findByStatusIdAndSerialNumberContainingIgnoreCase(
            UUID statusId, String search, Pageable pageable);
    Operation createNewOperation(Device device, Account account,
                                        String description);
    List<Operation> findBySerialNumber(String sn);
    Operation createNewDeviceVersion(Operation source, Account account,
                                  OperationStatus newStatus, String description);
    Map<String, String> getNEXT_STATUS_MAPPING();
}
