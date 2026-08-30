package ru.polymetal.labManufacture.service.account;

import ru.polymetal.labManufacture.data.models.Role;

import java.util.List;

/**
 * Контракт сервиса RoleService.
 *
 * @author Tatarinov Anton
 */
public interface RoleService {

    /**
     * Находит роль по её системному имени.
     *
     * @param name системное имя роли
     * @return найденная роль
     */
    Role findByName(String name);

    /**
     * Создаёт новую роль.
     *
     * @param name системное имя роли
     * @param description описание роли
     * @return созданная роль
     */
    Role createRole(String name, String description);

    /**
     * Возвращает все зарегистрированные роли.
     *
     * @return список ролей
     */
    List<Role> findAll();

    /**
     * Находит роли по набору системных имён.
     *
     * @param roleNames системные имена ролей
     * @return список найденных ролей
     */
    List<Role> findByNames(List<String> roleNames);
}
