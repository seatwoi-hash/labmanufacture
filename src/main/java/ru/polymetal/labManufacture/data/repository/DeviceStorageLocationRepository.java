package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.DeviceStorageLocation;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий доступа к данным DeviceStorageLocationRepository.
 *
 * @author Tatarinov Anton
 */
@Repository
public interface DeviceStorageLocationRepository extends JpaRepository<DeviceStorageLocation, UUID> {

    List<DeviceStorageLocation> findByDeviceId(UUID deviceId);

    List<DeviceStorageLocation> findByLocationTypeId(UUID locationTypeId);

    List<DeviceStorageLocation> findByIsCurrent(Boolean isCurrent);

}
