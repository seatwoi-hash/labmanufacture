package ru.polymetal.labManufacture.service.account.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.data.repository.RoleRepository;
import java.util.List;

@Service
@Slf4j
public class RoleServiceImpl {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


    @Transactional
    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Роль не найдена: " + name));
    }

    @Transactional
    public Role createRole(String name, String description) {
        if (roleRepository.existsByName(name)) {
            log.warn("Отклонено создание роли: роль уже существует, role={}", name);
            throw new RuntimeException("Роль уже существует: " + name);
        }

        Role role = new Role(name, description);
        Role saved = roleRepository.save(role);
        log.info("Роль создана: roleId={}, role={}", saved.getId(), saved.getName());
        return saved;
    }


    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Role> findByNames(List<String> roleNames) {
        return roleRepository.findByNameIn(roleNames);
    }

}
