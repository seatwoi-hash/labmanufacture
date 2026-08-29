package ru.polymetal.labManufacture.service.account.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.service.account.AccountService;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public void save(Account account) {
        boolean creating = account.getId() == null;
        accountRepository.save(account);
        log.info("Аккаунт сохранён: accountId={}, username={}, operation={}",
                account.getId(), account.getUsername(), creating ? "create" : "update");
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
        log.info("Изменение статуса аккаунта: accountId={}, active={}", accountId, status);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException(  // создать исключение именное
                        "Пользователь не найден"));
        account.setActive(status);
        accountRepository.save(account);
        log.info("Статус аккаунта изменён: accountId={}, active={}", accountId, status);


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
        log.info("Начата деактивация аккаунта: accountId={}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Аккаунт не найден"));

        account.setActive(false);
        log.info("Аккаунт деактивирован: accountId={}, username={}", id, account.getUsername());
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }


}
