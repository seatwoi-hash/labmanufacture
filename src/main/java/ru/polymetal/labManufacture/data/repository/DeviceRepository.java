package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.Device;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    List<Device> findBySerialNumber(String serialNumber);


    List<Device> findBySerialNumberContainingIgnoreCase(String serialNumber);

    List<Device> findByAccountId(UUID accountId);

    List<Device> findByTypeId(UUID typeId);
    boolean existsBySerialNumber(String serialNumber);

    List<Device> findByStatusId(UUID statusId);

    List<Device> findByIsDeleted(Boolean isDeleted);
    int countByCreatedTimeBetweenAndTypeNameAndStatusNameAndIsDeletedFalse(
            LocalDateTime start,
            LocalDateTime end,
            String typeName,
            String statusName
    );

    List<Device> findByStatusIdInAndIsDeleted(List<UUID> list, Boolean isDeleted);

    @Query("SELECT d FROM Device d LEFT JOIN FETCH d.status WHERE d.status.id IN :statusIds AND d.isDeleted = :isDeleted")
    List<Device> findByStatusIdInAndIsDeleted(@Param("statusIds") List<UUID> statusIds,
                                              @Param("isDeleted") boolean isDeleted);

    @Query("SELECT d FROM Device d LEFT JOIN FETCH d.status LEFT JOIN FETCH d.type WHERE d.status.id = :statusId AND d.isDeleted = false")
    List<Device> findByStatusIdAndDeletedWithFetch(@Param("statusId") UUID statusId);

    @Query("SELECT d FROM Device d " +
            "LEFT JOIN FETCH d.status " +
            "LEFT JOIN FETCH d.type " +
            "LEFT JOIN FETCH d.subType " +
            "LEFT JOIN FETCH d.account " +
            "WHERE d.status.id IN :statusIds AND d.isDeleted = :isDeleted " +
            "ORDER BY d.createdTime DESC")
    List<Device> findByStatusIdInAndIsDeletedWithFetch(
            @Param("statusIds") List<UUID> statusIds,
            @Param("isDeleted") boolean isDeleted
    );

    @Query("SELECT d FROM Device d " +
            "LEFT JOIN FETCH d.status " +
            "LEFT JOIN FETCH d.type " +
            "LEFT JOIN FETCH d.subType " +
            "LEFT JOIN FETCH d.account " +
            "WHERE d.id = :id")
    Optional<Device> findByIdWithFetch(@Param("id") UUID id);
}
