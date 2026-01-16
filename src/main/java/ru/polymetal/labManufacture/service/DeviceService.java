package ru.polymetal.labManufacture.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceStatus;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.dto.DeviceDto;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface DeviceService {

    void createDevice(DeviceDto deviceDto, String username);

    void validateDeviceDto(DeviceDto deviceDto);

    Device buildDevice(DeviceDto deviceDto, Account account, DeviceSubType subtype);

    List<Device> findDevicesForRole(Account account);

    Set<String> collectStatusNamesForRoles(Set<Role> roles);

    List<String> getAllStatusNames();

    List<UUID> getStatusIdsByNames(Collection<String> statusNames);


    void completeOperationWithDescription(UUID deviceId, Account account,
                                          String targetStatus, String description);

    void completeOperationWithoutDescription(UUID deviceId, Account account,
                                             String targetStatus);

    void performOperation(UUID deviceId, Account account,
                          String targetStatus, String description);

    Device createNewDeviceVersion(Device source, Account account,
                                  DeviceStatus newStatus, String description);

    void markDeviceAsDeleted(Device device);

    int getBoardsProducedToday();

    String getNextStatus(DeviceStatus status);

    List<Device> findByStatusIdAndIsDelete(UUID statusId);

    List<Device> findAll();

    boolean existsSerialNumber(String sn);

    List<Device> findBySerialNumber(String sn);

    // Новый метод для пагинации без поиска
    Page<Device> findByStatusId(UUID statusId, Pageable pageable);

    // Новый метод для пагинации с поиском
    Page<Device> findByStatusIdAndSerialNumberContainingIgnoreCase(
            UUID statusId, String search, Pageable pageable);

}
