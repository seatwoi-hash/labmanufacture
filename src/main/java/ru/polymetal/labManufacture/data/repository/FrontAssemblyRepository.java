package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.polymetal.labManufacture.data.models.FrontAssembly;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface FrontAssemblyRepository extends JpaRepository<FrontAssembly, UUID> {
    boolean existsByCaseIdIgnoreCaseAndIsDeletedFalse(String caseId);
    Optional<FrontAssembly> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM FrontAssembly a " +
            "WHERE a.isDeleted = false AND (a.motherboard.id = :deviceId OR a.keyboardBoard.id = :deviceId)")
    boolean existsActiveByBoardId(@Param("deviceId") UUID deviceId);

    @Query("SELECT a.motherboard.id FROM FrontAssembly a WHERE a.isDeleted = false")
    List<UUID> findActiveMotherboardIds();

    @Query("SELECT a.keyboardBoard.id FROM FrontAssembly a WHERE a.isDeleted = false")
    List<UUID> findActiveKeyboardBoardIds();
}
