package ru.polymetal.labManufacture.service.account.Impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.data.repository.RoleRepository;
import java.util.List;

@Service
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
            throw new RuntimeException("Роль уже существует: " + name);
        }

        Role role = new Role(name, description);
        return roleRepository.save(role);
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
