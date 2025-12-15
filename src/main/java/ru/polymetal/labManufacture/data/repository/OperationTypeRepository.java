package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.OperationType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperationTypeRepository extends JpaRepository<OperationType, UUID> {

    Optional<OperationType> findByName(String name);

    @Query("SELECT ot FROM OperationType ot WHERE ot.name IN :names")
    List<OperationType> findByNames(@Param("names") List<String> names);

    boolean existsByName(String name);

}
