package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.OperationStatusRoute;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий маршрутов производственных статусов.
 *
 * @author Tatarinov Anton
 */
@Repository
public interface OperationStatusRouteRepository extends JpaRepository<OperationStatusRoute, UUID> {

    @Query("""
            SELECT route
            FROM OperationStatusRoute route
            JOIN FETCH route.currentStatus
            ORDER BY route.currentStatus.name
            """)
    List<OperationStatusRoute> findAllWithCurrentStatus();

    Optional<OperationStatusRoute> findFirstByCurrentStatus_NameOrderById(String statusName);

    @Query("""
            SELECT CASE WHEN COUNT(route) > 0 THEN true ELSE false END
            FROM OperationStatusRoute route
            WHERE (route.previousStatus.id = :firstStatusId AND route.currentStatus.id = :secondStatusId)
               OR (route.previousStatus.id = :secondStatusId AND route.currentStatus.id = :firstStatusId)
               OR (route.currentStatus.id = :firstStatusId AND route.nextStatus.id = :secondStatusId)
               OR (route.currentStatus.id = :secondStatusId AND route.nextStatus.id = :firstStatusId)
            """)
    boolean areAdjacent(@Param("firstStatusId") UUID firstStatusId,
                        @Param("secondStatusId") UUID secondStatusId);
}
