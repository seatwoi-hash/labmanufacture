package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.DeviceStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceStatusRepository extends JpaRepository<DeviceStatus, UUID> {

    @Query("SELECT ds FROM DeviceStatus ds WHERE ds.name = :name")
    Optional<DeviceStatus> findByName(@Param("name") String name);
    @Query("SELECT ds FROM DeviceStatus ds " +
            "LEFT JOIN FETCH ds.devices " + // если есть связь с устройствами
            "WHERE ds.name = :name")
    Optional<DeviceStatus> findByNameWithDevices(@Param("name") String name);

    @Query("SELECT ds FROM DeviceStatus ds WHERE ds.name IN :names")
    List<DeviceStatus> findByNames(@Param("names") List<String> names);

    @Query("SELECT ds.id, ds.name FROM DeviceStatus ds WHERE ds.name IN :names")
    List<Object[]> findIdAndNameByNames(@Param("names") Collection<String> names);
    boolean existsByName(String name);

}
