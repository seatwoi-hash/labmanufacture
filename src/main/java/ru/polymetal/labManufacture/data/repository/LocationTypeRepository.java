package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.LocationType;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий доступа к данным LocationTypeRepository.
 *
 * @author Tatarinov Anton
 */
@Repository
public interface LocationTypeRepository extends JpaRepository<LocationType, UUID> {

    Optional<LocationType> findByTypeName(String typeName);

    List<LocationType> findByIsActive(Boolean isActive);

    List<LocationType> findByTypeNameContainingIgnoreCase(String typeName);

    boolean existsByTypeName(String typeName);

}
