package ru.polymetal.labManufacture.data.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.polymetal.labManufacture.data.models.FileData;
import ru.polymetal.labManufacture.data.models.Operation;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий доступа к данным FileDataRepository.
 *
 * @author Tatarinov Anton
 */
public interface FileDataRepository extends JpaRepository<FileData, UUID> {

    Optional<FileData> findByOperation(Operation operation);

    @Query("SELECT f FROM FileData f WHERE f.operation.id = :operationId")
    Optional<FileData> findByOperationId(@Param("operationId") UUID operationId);

    @Modifying
    @Query(value = "INSERT INTO file_data (original_name, new_name, mime_type, data, operations_id, account_id) " +
            "VALUES (:#{#fileData.originalName}, :#{#fileData.newName}, :#{#fileData.mimeType}, " +
            ":#{#fileData.data}, :#{#fileData.operation.id}, :#{#fileData.account.id})",
            nativeQuery = true)
    @Transactional
    void insertFileData(@Param("fileData") FileData fileData);
}
