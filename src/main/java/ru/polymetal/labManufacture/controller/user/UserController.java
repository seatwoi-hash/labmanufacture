package ru.polymetal.labManufacture.controller.user;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;


import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.dto.AccountCreateFormDto;
import ru.polymetal.labManufacture.dto.ProfileUpdateDto;
import ru.polymetal.labManufacture.service.account.AccountService;
import ru.polymetal.labManufacture.service.account.RoleService;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MVC-контроллер UserController.
 *
 * @author Tatarinov Anton
 */
@Controller
public class UserController {

    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final AccountService accountService;

    public UserController(PasswordEncoder passwordEncoder, RoleService roleService, AccountService accountService) {
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.accountService = accountService;
    }

    @GetMapping("/add-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String addUser(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());

        }

        return "usermenu/add-user";
    }

    @PostMapping("/admin/users/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addUser(@Valid @ModelAttribute("user") AccountCreateFormDto userFormDto,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        model.addAttribute("user", userFormDto);
        model.addAttribute("allRoles", roleService.findAll());

        Account user = new Account();
        user.setUsername(userFormDto.getUsername());
        user.setEmail(userFormDto.getEmail());
        user.setFirstName(userFormDto.getFirstName());
        user.setMiddleName(userFormDto.getMiddleName());
        user.setLastName(userFormDto.getLastName());
        user.setPasswordHash(passwordEncoder.encode(userFormDto.getPassword()));

        // Установка ролей
        Set<Role> roles = userFormDto.getRoles().stream()
                .map(roleService::findByName)
                .filter(Objects::nonNull) // фильтруем null
                .collect(Collectors.toSet());
        user.setRoles(roles);

        accountService.save(user);

        return "redirect:/main";

    }

    @GetMapping("/editing-user")
    public String editUser(Authentication authentication, Model model) {
        String username = authentication.getName();
        Account user = accountService.findByUsername(username);

        // Создай DTO с текущими данными пользователя
        ProfileUpdateDto request = new ProfileUpdateDto();
        request.setEmail(user.getEmail());

        model.addAttribute("user", user);
        model.addAttribute("request", request);

        return "usermenu/editing-user";
    }

    @PatchMapping("/profile/update")
    public String updateProfile(
            Authentication authentication,
            @Valid @ModelAttribute("request") ProfileUpdateDto request,
            BindingResult bindingResult,
            Model model) {

        String username = authentication.getName();
        Account user = accountService.findByUsername(username);

        // Проверка уникальности email (только если email изменился)
        if (!user.getEmail().equals(request.getEmail()) &&
                accountService.existsByEmail(request.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "Пользователь с таким email уже существует");
        }

        // Валидация пароля (только если он указан)
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            // Проверка длины пароля
            if (request.getNewPassword().length() < 6) {
                bindingResult.rejectValue("newPassword", "error.newPassword",
                        "Пароль должен содержать минимум 6 символов");
            }

            // Проверка совпадения паролей
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
                        "Новый пароль и подтверждение не совпадают");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("request", request);
            return "usermenu/editing-user";
        }

        user.setEmail(request.getEmail());

        // Меняем пароль только если указан новый пароль
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                model.addAttribute("error", "Для смены пароля необходимо ввести текущий пароль");
                model.addAttribute("user", user);
                model.addAttribute("request", request);
                return "usermenu/editing-user";
            }

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                model.addAttribute("error", "Текущий пароль введен неверно");
                model.addAttribute("user", user);
                model.addAttribute("request", request);
                return "usermenu/editing-user";
            }

            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        accountService.save(user);

        model.addAttribute("success", true);
        model.addAttribute("user", user);
        // Создаем новый DTO для очистки формы, но сохраняем email
        ProfileUpdateDto newDto = new ProfileUpdateDto();
        newDto.setEmail(user.getEmail()); // Сохраняем текущий email
        model.addAttribute("request", newDto);

        return "usermenu/editing-user";
    }


    @GetMapping("/employee")
    public String getUsersPage(Model model) {
        List<Account> accounts = accountService.findAllUsers();
        model.addAttribute("accounts", accounts);
        return "usermenu/employee";
    }

    @GetMapping("/editing-user/{id}")
    public String editUserAccount(@PathVariable("id") UUID id, Model model) {
        Account user = accountService.findById(id);

        // Преобразуем в DTO для формы
        AccountCreateFormDto userDto = new AccountCreateFormDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setMiddleName(user.getMiddleName());
        userDto.setLastName(user.getLastName());

        // Преобразуем роли в Set<String> для чекбоксов
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        userDto.setRoles(roleNames);

        model.addAttribute("user", userDto);
        return "usermenu/edit-account";
    }

    @PostMapping("/admin/users/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editUser(@Valid @ModelAttribute("user") AccountCreateFormDto accountCreateFormDto,
                           BindingResult result,
                           Model model) {

        // Проверка существования пользователя
        Account user = accountService.findById(accountCreateFormDto.getId());
        if (user == null) {
            result.rejectValue("id", "error.user", "User not found");
            model.addAttribute("allRoles", roleService.findAll());
            model.addAttribute("error", "Пользователь не найден");
            return "usermenu/edit-account";
        }

        // Проверка email на уникальность (если email изменился)
        if (!user.getEmail().equalsIgnoreCase(accountCreateFormDto.getEmail()) &&
                accountService.existsByEmail(accountCreateFormDto.getEmail())) {
            result.rejectValue("email", "error.email", "Пользователь с таким email уже существует");
            model.addAttribute("error",  "Пользователь с таким email уже существует");
        }

        // Проверка username на уникальность (если username изменился)
        if (!user.getUsername().equalsIgnoreCase(accountCreateFormDto.getUsername()) &&
                accountService.existsByUsername(accountCreateFormDto.getUsername())) {
            result.rejectValue("username", "error.username", "Пользователь с таким логином уже существует");
            model.addAttribute("error", "Пользователь с таким логином уже существует");
        }

        // Проверка пароля ТОЛЬКО если он указан
        if (accountCreateFormDto.isPasswordPresent()) {
            // Проверяем совпадение паролей
            if (!accountCreateFormDto.getPassword().equals(accountCreateFormDto.getConfirmPassword())) {
                result.rejectValue("confirmPassword", "error.confirmPassword",
                        "Пароли не совпадают");
                model.addAttribute("error", "Пароли не совпадают");
            }
            // Проверяем длину пароля
            else if (accountCreateFormDto.getPassword().length() < 6) {
                result.rejectValue("password", "error.password",
                        "Пароль должен содержать минимум 6 символов");
                model.addAttribute("error", "Пароль должен содержать минимум 6 символов");
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("allRoles", roleService.findAll());

            return "usermenu/edit-account";
        }

        // Обновление данных пользователя
        user.setUsername(accountCreateFormDto.getUsername());
        user.setEmail(accountCreateFormDto.getEmail());
        user.setFirstName(accountCreateFormDto.getFirstName());
        user.setMiddleName(accountCreateFormDto.getMiddleName());
        user.setLastName(accountCreateFormDto.getLastName());

        if (accountCreateFormDto.isPasswordPresent()) {
            user.setPasswordHash(passwordEncoder.encode(accountCreateFormDto.getPassword()));
        }

        if (accountCreateFormDto.getRoles() != null) {
            Set<Role> roles = accountCreateFormDto.getRoles().stream()
                    .map(roleService::findByName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        accountService.save(user);
        model.addAttribute("success", true);
        model.addAttribute("allRoles", roleService.findAll());

        return "usermenu/edit-account";
    }


    @DeleteMapping("/delete/account/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable UUID id)
    {

        accountService.delete(id);

        return "redirect:/employee";
    }
}
