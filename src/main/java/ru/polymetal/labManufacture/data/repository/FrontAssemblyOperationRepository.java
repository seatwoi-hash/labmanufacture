package ru.polymetal.labManufacture.data.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.polymetal.labManufacture.data.models.FrontAssemblyOperation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FrontAssemblyOperationRepository extends JpaRepository<FrontAssemblyOperation, UUID> {
    @Query("select o from FrontAssemblyOperation o join fetch o.frontAssembly a " +
            "join fetch a.motherboard join fetch a.keyboardBoard join fetch o.status " +
            "where o.isDeleted = false and a.isDeleted = false and o.status.name in :statuses " +
            "order by o.createdTime asc")
    List<FrontAssemblyOperation> findActiveByStatusNames(@Param("statuses") Collection<String> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from FrontAssemblyOperation o join fetch o.status " +
            "where o.frontAssembly.id = :assemblyId and o.isDeleted = false")
    Optional<FrontAssemblyOperation> findActiveForUpdate(@Param("assemblyId") UUID assemblyId);
}
