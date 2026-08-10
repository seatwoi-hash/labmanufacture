package ru.polymetal.labManufacture.controller.devicetype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.dto.DeviceSubTypeDto;
import ru.polymetal.labManufacture.exception.DeviceTypeNotFoundException;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.nextcloud.LinkService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
@RequestMapping("/devicetype")
public class DeviceTypeController {

    private final DeviceSubTypeService deviceSubTypeService;
    private final AccountRepository accountRepository;



    public DeviceTypeController(DeviceSubTypeService deviceSubTypeService, AccountRepository accountRepository) {
        this.deviceSubTypeService = deviceSubTypeService;
        this.accountRepository = accountRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @GetMapping("/add")
    public String showCreateDeviceForm(Model model, Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);

        List<DeviceSubType> deviceSubTypes = deviceSubTypeService.findAll().stream()
                .filter(a -> !a.getIsDeleted()).toList();

        model.addAttribute("subtypeList", deviceSubTypes);

        return "usermenu/add-type-devices";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @PostMapping("/add")
    public String addType(   @RequestParam String name,
                             @RequestParam String description,
                             @RequestParam Integer snType,
                             @RequestParam Integer versionType,
                             @RequestParam(required = false, defaultValue = "false") Boolean isInstallationOne,
                             @RequestParam(required = false, defaultValue = "false") Boolean isTestTwo,
                             @RequestParam(required = false, defaultValue = "false") Boolean isSideTwo,
                             @RequestParam(value = "file", required = false) MultipartFile file,
                             @RequestParam(value = "zip", required = false) MultipartFile zip) {

        DeviceSubTypeDto deviceSubTypeDto = DeviceSubTypeDto.builder()
                .name(name)
                .description(description)
                .snType(snType)
                .versionType(versionType)
                .isInstallationOne(isInstallationOne)
                .isTestTwo(isTestTwo)
                .isSideTwo(isSideTwo).
                build();

        try {
            deviceSubTypeService.save(deviceSubTypeDto, file, zip);

        } catch (RuntimeException e) {
            log.error("Ошибка сохранения DeviceSubType", e);

            return "usermenu/add-type-devices";
        } catch (IOException e) {
            log.error("Ошибка сохранения DeviceSubType", e);

            throw new RuntimeException(e);
        }

        return "redirect:/devicetype/add";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @DeleteMapping("/delete/{id}")
    public String deleteType(@PathVariable UUID id)
    {

        deviceSubTypeService.delete(id);

        return "redirect:/devicetype/add";
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @GetMapping("/edite/{id}")
    public String showEditeDeviceForm(Model model, Authentication authentication, @PathVariable UUID id) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);

        DeviceSubType deviceSubType = deviceSubTypeService.findById(id).orElseThrow(DeviceTypeNotFoundException::new);
        model.addAttribute("subtype", deviceSubType);

        return "usermenu/edite-type-devices";
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @PatchMapping("/edite/{id}")
    public String editeType(DeviceSubTypeDto deviceSubTypeDto,
                            @PathVariable UUID id,
                            @RequestParam(value = "file", required = false) MultipartFile file,
                            @RequestParam(value = "zip", required = false) MultipartFile zip) throws IOException {

        deviceSubTypeService.edite(deviceSubTypeDto, id, file, zip);
        log.info("после");
        return "redirect:/devicetype/add";
    }



    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {

        DeviceSubType deviceSubType =
                deviceSubTypeService.findById(id).orElseThrow(DeviceTypeNotFoundException::new);

        byte[] data = deviceSubType.getData();

        if (data == null || data.length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"altium-" + deviceSubType.getName() + ".zip\""
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(data);
    }

}
