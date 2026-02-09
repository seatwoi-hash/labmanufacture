package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.polymetal.labManufacture.data.models.Device;
import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    List<Device> findBySerialNumber(String serialNumber);

    List<Device> findBySerialNumberContainingIgnoreCase(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);
    boolean existsBySerialNumberAndIsDeletedFalse(String serialNumber);


    List<Device> findByTypeId(UUID typeId);

}
