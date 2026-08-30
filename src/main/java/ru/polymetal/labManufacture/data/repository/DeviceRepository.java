package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.polymetal.labManufacture.data.models.Device;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий доступа к данным DeviceRepository.
 *
 * @author Tatarinov Anton
 */
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Optional<Device> findBySerialNumber(String serialNumber);

    List<Device> findBySerialNumberContainingIgnoreCase(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);
    boolean existsBySerialNumberAndIsDeletedFalse(String serialNumber);

    Optional<Device> findByIdAndIsDeletedFalse(UUID id);


    List<Device> findByTypeId(UUID typeId);


    Optional<Device> findOneBySerialNumberAndIsDeletedFalse(String serialNumber);

}
