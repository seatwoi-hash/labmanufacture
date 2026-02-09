package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.OperationStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperationStatusRepository extends JpaRepository<OperationStatus, UUID> {

    @Query("SELECT ds FROM OperationStatus ds WHERE ds.name = :name")
    Optional<OperationStatus> findByName(@Param("name") String name);
    @Query("SELECT ds FROM OperationStatus ds " +
            "LEFT JOIN FETCH ds.devices " +
            "WHERE ds.name = :name")
    Optional<OperationStatus> findByNameWithDevices(@Param("name") String name);

    @Query("SELECT ds FROM OperationStatus ds WHERE ds.name IN :names")
    List<OperationStatus> findByNames(@Param("names") List<String> names);

    @Query("SELECT ds.id, ds.name FROM OperationStatus ds WHERE ds.name IN :names")
    List<Object[]> findIdAndNameByNames(@Param("names") Collection<String> names);
    boolean existsByName(String name);

}
