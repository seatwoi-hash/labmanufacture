package ru.polymetal.labManufacture.service.account;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.service.AccountService;
import java.util.List;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public void save(Account account) {
        accountRepository.save(account);
    }

    @Override
    public Account findByUsername(String name) {
        return accountRepository.findByUsername(name).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> findAllUsers() {
        return accountRepository.findAll().stream().filter(Account::getActive).toList();
    }

    @Override
    @Transactional
    public void updateUserStatus(UUID accountId, Boolean status) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException(  // создать исключение именное
                        "Пользователь не найден"));
        account.setActive(status);
        accountRepository.save(account);


    }

    @Override
    @Transactional(readOnly = true)
    public Account findById(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException(  // создать исключение именное
                        "Пользователь не найден"));

        return account;
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Аккаунт не найден"));

        account.setActive(false);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }


}
