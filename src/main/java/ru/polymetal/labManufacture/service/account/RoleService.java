package ru.polymetal.labManufacture.service.account;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.data.repository.RoleRepository;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RoleService{
    private final RoleRepository roleRepository;
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Роль не найдена: " + name));
    }

    public Role createRole(String name, String description) {
        if (roleRepository.existsByName(name)) {
            throw new RuntimeException("Роль уже существует: " + name);
        }

        Role role = new Role(name, description);
        return roleRepository.save(role);
    }


    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public List<Role> findByNames(List<String> roleNames) {
        return roleRepository.findByNameIn(roleNames);
    }

}
