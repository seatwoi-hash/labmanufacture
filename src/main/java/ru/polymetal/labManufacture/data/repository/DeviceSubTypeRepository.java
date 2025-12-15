package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.DeviceType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceSubTypeRepository extends JpaRepository<DeviceSubType, UUID> {

    Optional<DeviceSubType> findByName(String name);

    List<DeviceSubType> findByNameContainingIgnoreCase(String name);

    List<DeviceSubType> findAll();

    boolean existsByName(String name);

}

