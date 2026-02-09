package ru.polymetal.labManufacture.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.dto.DeviceSubTypeDto;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/devicetype")
public class DeviceTypeController {

    private final DeviceSubTypeService deviceSubTypeService;
    private final AccountRepository accountRepository;


    public DeviceTypeController(DeviceSubTypeService deviceSubTypeService, AccountRepository accountRepository) {
        this.deviceSubTypeService = deviceSubTypeService;
        this.accountRepository = accountRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/add")
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
                             @RequestParam String description,
                             @RequestParam(required = false, defaultValue = "false") Boolean isInstallationOne,
                             @RequestParam(required = false, defaultValue = "false") Boolean isTestTwo) {

        try {
            deviceSubTypeService.save(name, description, isInstallationOne, isTestTwo);
        } catch (RuntimeException e) {
            return "usermenu/add-type-devices";
        }

        return "redirect:/devicetype/add";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public String deleteType(@PathVariable UUID id)
    {

        deviceSubTypeService.delete(id);

        return "redirect:/devicetype/add";
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edite/{id}")
    public String showEditeDeviceForm(Model model, Authentication authentication, @PathVariable UUID id) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        DeviceSubType deviceSubType = deviceSubTypeService.findById(id).orElseThrow(() -> new RuntimeException(
                "Тип платы не найден"));
        model.addAttribute("subtype", deviceSubType);

        return "usermenu/edite-type-devices";
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/edite/{id}")
    public String editeType(DeviceSubTypeDto deviceSubTypeDto, @PathVariable UUID id)
    {

        deviceSubTypeService.edite(deviceSubTypeDto, id);

        return "redirect:/devicetype/add";
    }

}
