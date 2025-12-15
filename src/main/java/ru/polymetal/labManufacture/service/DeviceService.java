package ru.polymetal.labManufacture.service;

import jakarta.transaction.Transactional;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceStatus;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.dto.DeviceDto;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DeviceService {

    @Transactional
    void createDevice(DeviceDto deviceDto, String username);

    void validateDeviceDto(DeviceDto deviceDto);

    Device buildDevice(DeviceDto deviceDto, Account account, DeviceSubType subtype);

    List<Device> findDevicesForRole(Account account);

    Set<String> collectStatusNamesForRoles(Set<Role> roles);

    List<String> getAllStatusNames();

    List<UUID> getStatusIdsByNames(Collection<String> statusNames);

    @Transactional
    void completeOperationWithDescription(UUID deviceId, Account account,
                                          String targetStatus, String description);

    @Transactional
    void completeOperationWithoutDescription(UUID deviceId, Account account,
                                             String targetStatus);

    @Transactional
    void completeMOne(UUID deviceId, Account account);

    @Transactional
    void completeMTwo(UUID deviceId, Account account);

    @Transactional
    void completeOTKOne(UUID deviceId, Account account, String description);

    @Transactional
    void failOTKOne(UUID deviceId, Account account, String description);

    void performOperation(UUID deviceId, Account account,
                          String targetStatus, String description);

    Device createNewDeviceVersion(Device source, Account account,
                                  DeviceStatus newStatus, String description);

    void markDeviceAsDeleted(Device device);

    int getBoardsProducedToday();

    String getNextStatus(DeviceStatus status);

    Device createDevice(Device device, Account account);

    void validateDevice(Device device);

    List<Device> findByAccount(Account account);

    Optional<Device> findById(UUID id);

    List<Device> findByStatusId(UUID statusId);

    List<Device> findByStatusIdAndIsDelete(UUID statusId);

    List<Device> findByStatusIdAndIsDelete(List<UUID> statusIds);

    List<Device> findAll();

    boolean existsSerialNumber(String sn);

    List<Device> findBySerialNumberContainingIgnoreCase(String sn);

}
