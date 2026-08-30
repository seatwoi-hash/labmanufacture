package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import ru.polymetal.labManufacture.data.models.OperationStatus;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий матрицы доступа ролей к производственным статусам.
 *
 * @author Tatarinov Anton
 */
public interface RoleOperationStatusAccessRepository extends Repository<OperationStatus, UUID> {

    @Query(value = """
            SELECT DISTINCT access.operation_status_id
            FROM role_operation_status_access access
            WHERE access.role_id IN (:roleIds)
            """, nativeQuery = true)
    List<UUID> findOperationStatusIdsByRoleIds(@Param("roleIds") Collection<UUID> roleIds);
}
