package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.OpsStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OpsStatusRepository extends JpaRepository<OpsStatus, UUID> {

    Optional<OpsStatus> findByName(String name);

    @Query("SELECT os FROM OpsStatus os WHERE os.name IN :names")
    List<OpsStatus> findByNames(@Param("names") List<String> names);

    boolean existsByName(String name);

}

