package ru.polymetal.labManufacture.controller.devicetype;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.dto.DeviceSubTypeDto;
import ru.polymetal.labManufacture.exception.DeviceTypeNotFoundException;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.account.AccountService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@Controller
@Slf4j
@RequestMapping("/devicetype")
@PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
public class DeviceTypeController {

    private static final String ADD_VIEW = "usermenu/add-type-devices";
    private static final String EDIT_VIEW = "usermenu/edite-type-devices";
    private static final String REDIRECT_TO_LIST = "redirect:/devicetype/add";

    private final DeviceSubTypeService deviceSubTypeService;
    private final AccountService accountService;

    public DeviceTypeController(DeviceSubTypeService deviceSubTypeService, AccountService accountService) {
        this.deviceSubTypeService = deviceSubTypeService;
        this.accountService = accountService;
    }

    @GetMapping("/add")
    public String showCreateDeviceForm(Model model, Authentication authentication) {
        populateCommonModel(model, authentication);
        model.addAttribute("subtype", emptyForm());
        return ADD_VIEW;
    }

    @PostMapping("/add")
    public String addType(@Valid @ModelAttribute("subtype") DeviceSubTypeDto form,
                          BindingResult bindingResult,
                          @RequestParam(value = "file", required = false) MultipartFile file,
                          @RequestParam(value = "zip", required = false) MultipartFile zip,
                          Model model,
                          Authentication authentication) {
        if (bindingResult.hasErrors()) {
            populateCommonModel(model, authentication);
            return ADD_VIEW;
        }
        String uploadError = validateUploads(file, zip, true);
        if (uploadError != null) {
            return formError(model, authentication, uploadError, ADD_VIEW);
        }

        try {
            deviceSubTypeService.save(form, file, zip);
            return REDIRECT_TO_LIST;
        } catch (IOException e) {
            log.error("Ошибка загрузки файла для типа платы '{}'", form.name(), e);
            return formError(model, authentication, "Не удалось загрузить файл", ADD_VIEW);
        } catch (RuntimeException e) {
            log.warn("Не удалось сохранить тип платы '{}': {}", form.name(), e.getMessage());
            return formError(model, authentication, e.getMessage(), ADD_VIEW);
        }
    }

    @DeleteMapping("/delete/{id}")
    public String deleteType(@PathVariable UUID id) {
        deviceSubTypeService.delete(id);
        return REDIRECT_TO_LIST;
    }

    @GetMapping({"/edite/{id}", "/edit/{id}"})
    public String showEditDeviceForm(Model model, Authentication authentication, @PathVariable UUID id) {
        model.addAttribute("currentUser", requireCurrentAccount(authentication));
        model.addAttribute("subtype", DeviceSubTypeDto.from(findSubtype(id)));
        return EDIT_VIEW;
    }

    @PatchMapping({"/edite/{id}", "/edit/{id}"})
    public String editType(@Valid @ModelAttribute("subtype") DeviceSubTypeDto form,
                           BindingResult bindingResult,
                           @PathVariable UUID id,
                           @RequestParam(value = "file", required = false) MultipartFile file,
                           @RequestParam(value = "zip", required = false) MultipartFile zip,
                           Model model,
                           Authentication authentication) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", requireCurrentAccount(authentication));
            return EDIT_VIEW;
        }
        String uploadError = validateUploads(file, zip, false);
        if (uploadError != null) {
            return formError(model, authentication, uploadError, EDIT_VIEW);
        }

        try {
            deviceSubTypeService.edit(form, id, file, zip);
            return REDIRECT_TO_LIST;
        } catch (IOException e) {
            log.error("Ошибка загрузки файла при редактировании типа платы '{}'", id, e);
            return formError(model, authentication, "Не удалось загрузить файл", EDIT_VIEW);
        } catch (RuntimeException e) {
            log.warn("Не удалось изменить тип платы '{}': {}", id, e.getMessage());
            return formError(model, authentication, e.getMessage(), EDIT_VIEW);
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {
        DeviceSubType subtype = findSubtype(id);
        byte[] data = subtype.getData();
        if (data == null || data.length == 0) {
            return ResponseEntity.notFound().build();
        }

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("altium-" + safeFilename(subtype.getName()) + ".zip", StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(data);
    }

    private String formError(Model model, Authentication authentication, String error, String view) {
        model.addAttribute("currentUser", requireCurrentAccount(authentication));
        if (ADD_VIEW.equals(view)) {
            model.addAttribute("subtypeList", deviceSubTypeService.findAll());
        }
        model.addAttribute("error", error);
        return view;
    }

    private void populateCommonModel(Model model, Authentication authentication) {
        model.addAttribute("currentUser", requireCurrentAccount(authentication));
        model.addAttribute("subtypeList", deviceSubTypeService.findAll());
    }

    private Account requireCurrentAccount(Authentication authentication) {
        Account account = accountService.findByUsername(authentication.getName());
        if (account == null) {
            throw new UserNotFoundException(authentication.getName());
        }
        return account;
    }

    private DeviceSubType findSubtype(UUID id) {
        return deviceSubTypeService.findById(id).orElseThrow(DeviceTypeNotFoundException::new);
    }

    private DeviceSubTypeDto emptyForm() {
        return DeviceSubTypeDto.builder()
                .isInstallationOne(true).isTestTwo(true).isSideTwo(true)
                .build();
    }

    private String safeFilename(String value) {
        return value.replaceAll("[\\r\\n\\\"\\\\/]", "_");
    }

    private String validateUploads(MultipartFile file, MultipartFile archive, boolean pdfRequired) {
        if (pdfRequired && (file == null || file.isEmpty())) {
            return "PDF-файл обязателен";
        }
        if (file != null && !file.isEmpty() && !hasExtension(file, ".pdf")) {
            return "Допустим только PDF-файл";
        }
        if (archive != null && !archive.isEmpty()
                && !hasExtension(archive, ".zip", ".rar", ".7z", ".tar", ".gz", ".tgz")) {
            return "Недопустимый формат архива";
        }
        return null;
    }

    private boolean hasExtension(MultipartFile file, String... extensions) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        String normalized = filename.toLowerCase(Locale.ROOT);
        for (String extension : extensions) {
            if (normalized.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}
