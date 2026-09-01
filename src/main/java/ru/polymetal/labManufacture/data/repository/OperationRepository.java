package ru.polymetal.labManufacture.data.repository;

import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.Operation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий доступа к данным OperationRepository.
 *
 * @author Tatarinov Anton
 */
@Repository
public interface OperationRepository extends JpaRepository<Operation, UUID> {

    List<Operation> findByAccountId(UUID accountId);
    Optional<Operation> findById(@NotNull UUID operationId);

    List<Operation> findByIsDeleted(Boolean isDeleted);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Operation o WHERE o.id = :id AND o.isDeleted = false")
    Optional<Operation> findByIdWithLock(@Param("id") UUID id);

    int countByCreatedTimeBetweenAndDevice_Type_NameAndStatus_NameAndIsDeletedFalse(
            LocalDateTime start,
            LocalDateTime end,
            String typeName,
            String statusName
    );
    List<Operation> findByStatusId(UUID statusId);

    List<Operation> findByStatusIdInAndIsDeleted(List<UUID> list, Boolean isDeleted);

    @Query("SELECT o FROM Operation o LEFT JOIN FETCH o.status WHERE o.status.id IN :statusIds AND o.isDeleted = :isDeleted")
    List<Operation> findByStatusIdInAndIsDeleted(@Param("statusIds") List<UUID> statusIds,
                                                 @Param("isDeleted") boolean isDeleted);

    @Query("SELECT o FROM Operation o " +
            "LEFT JOIN FETCH o.status " +
            "LEFT JOIN FETCH o.device d " +
            "LEFT JOIN FETCH d.type " +
            "WHERE o.status.id = :statusId " +
            "AND o.isDeleted = false " +
            "AND COALESCE(d.isDeleted, false) = false")
    List<Operation> findByStatusIdAndDeletedWithFetch(@Param("statusId") UUID statusId);

    @Query("SELECT o FROM Operation o " +
            "LEFT JOIN FETCH o.status " +
            "LEFT JOIN FETCH o.account " +
            "LEFT JOIN FETCH o.device d " +
            "LEFT JOIN FETCH d.type " +
            "LEFT JOIN FETCH d.subtype " +
            "WHERE o.status.id IN :statusIds AND o.isDeleted = :isDeleted " +
            "AND (d IS NULL OR d.isDeleted = false) " +
            "ORDER BY d.createdTime DESC")
    List<Operation> findByStatusIdInAndIsDeletedWithFetch(
            @Param("statusIds") List<UUID> statusIds,
            @Param("isDeleted") boolean isDeleted
    );

    @Query("SELECT o FROM Operation o " +
            "LEFT JOIN FETCH o.status " +
            "LEFT JOIN FETCH o.account " +
            "LEFT JOIN FETCH o.device d " +
            "LEFT JOIN FETCH d.type " +
            "LEFT JOIN FETCH d.subtype " +
            "WHERE d.id = :id")
    Optional<Operation> findByIdWithFetch(@Param("id") UUID id);

    Page<Operation> findByStatusIdAndIsDeleted(UUID statusId, Boolean isDelete, Pageable pageable);

    @Query("SELECT o FROM Operation o " +
            "JOIN o.device d " +
            "WHERE o.status.id = :statusId " +
            "AND o.isDeleted = :isDeleted " +
            "AND LOWER(d.serialNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Operation> findByStatusIdAndSerialNumberContainingIgnoreCase(
            @Param("statusId") UUID statusId,
            @Param("search") String search,
            @Param("isDeleted") Boolean isDeleted,
            Pageable pageable);

    List<Operation> findByDevice_SerialNumber(String serialNumber);

    Optional<Operation> findByDeviceSerialNumberAndStatusName(
            String serialNumber,
            String statusName
    );

    Optional<Operation> findFirstByDevice_IdAndIsDeletedTrueOrderByDeletedAtDesc(UUID deviceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Operation o WHERE o.device.id = :deviceId AND o.isDeleted = false")
    Optional<Operation> findActiveByDeviceIdWithLock(@Param("deviceId") UUID deviceId);

    @Query("SELECT o FROM Operation o " +
            "JOIN FETCH o.status " +
            "JOIN FETCH o.account " +
            "WHERE o.device.id = :deviceId " +
            "ORDER BY o.createdTime DESC")
    List<Operation> findHistoryByDeviceId(@Param("deviceId") UUID deviceId);

    @Query("SELECT o FROM Operation o " +
            "JOIN FETCH o.device d " +
            "JOIN FETCH d.subtype " +
            "JOIN FETCH o.status " +
            "JOIN FETCH o.account " +
            "WHERE o.isDeleted = false " +
            "AND COALESCE(d.isDeleted, false) = false " +
            "ORDER BY o.createdTime DESC")
    List<Operation> findActiveOperationsForRollback();

    @Query("SELECT o FROM Operation o " +
            "JOIN FETCH o.device d " +
            "JOIN FETCH o.status " +
            "WHERE o.isDeleted = true AND d.id IN :deviceIds " +
            "AND COALESCE(o.isRollback, false) = false " +
            "AND NOT EXISTS (SELECT rollbackOperation.id FROM Operation rollbackOperation " +
            "WHERE rollbackOperation.rolledBackOperationId = o.id) " +
            "ORDER BY o.createdTime DESC")
    List<Operation> findRollbackTargetsByDeviceIds(@Param("deviceIds") Collection<UUID> deviceIds);
}
