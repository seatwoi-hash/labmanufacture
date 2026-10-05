package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.polymetal.labManufacture.data.models.FrontAssemblyStatus;

import java.util.Optional;
import java.util.UUID;

public interface FrontAssemblyStatusRepository extends JpaRepository<FrontAssemblyStatus, UUID> {
    Optional<FrontAssemblyStatus> findByName(String name);
}
