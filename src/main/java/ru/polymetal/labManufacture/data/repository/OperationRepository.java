package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.Operation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OperationRepository extends JpaRepository<Operation, UUID> {

    List<Operation> findByDeviceId(UUID deviceId);

    List<Operation> findByAccountId(UUID accountId);

    List<Operation> findByOperationTypeId(UUID operationTypeId);

    List<Operation> findByOpsStatusId(UUID opsStatusId);

    List<Operation> findByIsDeleted(Boolean isDeleted);

    List<Operation> findByOperationTimeBetween(LocalDateTime start, LocalDateTime end);

}
