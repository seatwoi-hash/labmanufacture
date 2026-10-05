package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.polymetal.labManufacture.data.models.FrontAssemblyType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FrontAssemblyTypeRepository extends JpaRepository<FrontAssemblyType, UUID> {
    List<FrontAssemblyType> findAllByIsDeletedFalseOrderByNameAsc();

    Optional<FrontAssemblyType> findByNameIgnoreCaseAndIsDeletedFalse(String name);

    Optional<FrontAssemblyType> findByIdAndIsDeletedFalse(UUID id);
}
