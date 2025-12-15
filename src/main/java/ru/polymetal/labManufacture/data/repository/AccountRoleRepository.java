package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.AccountRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRoleRepository extends JpaRepository<AccountRole, UUID> {

    List<AccountRole> findByAccountId(UUID accountId);

    List<AccountRole> findByRoleId(UUID roleId);

    Optional<AccountRole> findByAccountIdAndRoleId(UUID accountId, UUID roleId);

    boolean existsByAccountIdAndRoleId(UUID accountId, UUID roleId);

}
