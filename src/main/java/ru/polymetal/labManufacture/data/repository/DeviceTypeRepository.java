package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.DeviceType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceTypeRepository extends JpaRepository<DeviceType, UUID> {

    Optional<DeviceType> findByName(String name);

    List<DeviceType> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String name);

}
