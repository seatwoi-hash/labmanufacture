package ru.polymetal.labManufacture.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/devicetype/edite")
public class DeviceTypeController {

    private final DeviceSubTypeService deviceSubTypeService;
    private final AccountRepository accountRepository;


    public DeviceTypeController(DeviceSubTypeService deviceSubTypeService, AccountRepository accountRepository) {
        this.deviceSubTypeService = deviceSubTypeService;
        this.accountRepository = accountRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String showCreateDeviceForm(Model model, Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        List<DeviceSubType> deviceSubTypes = deviceSubTypeService.findAll().stream()
                .filter(a -> !a.getIsDeleted()).toList();

        model.addAttribute("subtypeList", deviceSubTypes);

        return "usermenu/add-type-devices";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public String addType(   @RequestParam String name,
                             @RequestParam String description, Model model) {

        try {
            deviceSubTypeService.save(name, description);
        } catch (RuntimeException e) {
            return "usermenu/add-type-devices";
        }

        return "redirect:/devicetype/edite";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public String deleteType(@PathVariable UUID id)
    {

        deviceSubTypeService.delete(id);

        return "redirect:/devicetype/edite";
    }

}
