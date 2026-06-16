package ru.polymetal.labManufacture.service.account;

import ru.polymetal.labManufacture.data.models.Account;
import java.util.List;
import java.util.UUID;

public interface AccountService {

    void save(Account account);

    Account findByUsername(String name);

    List<Account> findAllUsers();

    void updateUserStatus(UUID accountId, Boolean status);

    Account findById(UUID accountId);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    void delete(UUID id);

}
