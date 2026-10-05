package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.polymetal.labManufacture.data.models.FrontAssembly;

import java.util.Optional;
import java.util.UUID;

public interface FrontAssemblyRepository extends JpaRepository<FrontAssembly, UUID> {
    boolean existsByCaseIdIgnoreCaseAndIsDeletedFalse(String caseId);
    Optional<FrontAssembly> findByIdAndIsDeletedFalse(UUID id);
}
