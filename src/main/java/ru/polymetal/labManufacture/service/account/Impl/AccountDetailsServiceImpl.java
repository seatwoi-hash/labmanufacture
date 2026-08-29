package ru.polymetal.labManufacture.service.account.Impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.security.AccountDetails;
import ru.polymetal.labManufacture.service.account.AccountDetailsService;
import java.util.Set;


@Service
@Transactional
public class AccountDetailsServiceImpl implements AccountDetailsService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;


    public AccountDetailsServiceImpl(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return new AccountDetails(account);
    }

    private String[] getAuthorities(Set<Role> roles) {
        // Преобразуем роли в authorities с префиксом ROLE_
        return roles.stream()
                .map(role -> "ROLE_" + role.getName().toUpperCase())
                .toArray(String[]::new);
    }

}
