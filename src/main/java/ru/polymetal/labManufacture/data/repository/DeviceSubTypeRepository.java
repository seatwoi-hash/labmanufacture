package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<DeviceSubType> findAllByName(String name);

    boolean existsByName(String name);

    @Query("SELECT d.isInstallationOne FROM DeviceSubType d WHERE d.id = :id AND d.isDeleted = false")
    Boolean findIsInstallationOneByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT d.isTestTwo FROM DeviceSubType d WHERE d.id = :id AND d.isDeleted = false")
    Boolean findIsTestTwoByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT d.isSideTwo FROM DeviceSubType d WHERE d.id = :id AND d.isDeleted = false")
    Boolean findIsSideTwoByIdAndNotDeleted(@Param("id") UUID id);
}

