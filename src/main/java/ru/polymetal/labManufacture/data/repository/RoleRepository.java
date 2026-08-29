package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий доступа к данным RoleRepository.
 *
 * @author Tatarinov Anton
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);

    List<Role> findByIsDefault(Boolean isDefault);

    List<Role> findByNameIn(List<String> names);


    boolean existsByName(String name);

}
