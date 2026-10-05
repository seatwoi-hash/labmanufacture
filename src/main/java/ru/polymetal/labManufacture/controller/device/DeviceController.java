package ru.polymetal.labManufacture.controller.device;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.dto.DeviceDto;
import ru.polymetal.labManufacture.dto.FrontAssemblyTypeDto;
import ru.polymetal.labManufacture.dto.FrontAssemblyCreateDto;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.device.DeviceService;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.FrontAssemblyTypeService;
import ru.polymetal.labManufacture.service.FrontAssemblyFlowService;
import ru.polymetal.labManufacture.service.nextcloud.LinkService;
import ru.polymetal.labManufacture.service.operation.OperationQueryService;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.READY;

/**
 * MVC-контроллер DeviceController.
 *
 * @author Tatarinov Anton
 */
@Controller
@RequestMapping("/device")
public class DeviceController {

    private final AccountRepository accountRepository;
    private final DeviceService deviceService;
    private final DeviceSubTypeService deviceSubTypeService;
    private final FrontAssemblyTypeService frontAssemblyTypeService;
    private final FrontAssemblyFlowService frontAssemblyFlowService;
    private final OperationQueryService operationQueryService;
    private final LinkService linkService;


    public DeviceController(AccountRepository accountRepository, DeviceService deviceService,
                            DeviceSubTypeService deviceSubTypeService,
                            FrontAssemblyTypeService frontAssemblyTypeService,
                            FrontAssemblyFlowService frontAssemblyFlowService,
                            OperationQueryService operationQueryService,
                            LinkService linkService) {
        this.accountRepository = accountRepository;
        this.deviceService = deviceService;
        this.deviceSubTypeService = deviceSubTypeService;
        this.frontAssemblyTypeService = frontAssemblyTypeService;
        this.frontAssemblyFlowService = frontAssemblyFlowService;
        this.operationQueryService = operationQueryService;
        this.linkService = linkService;
    }


    @GetMapping("/create-board")
    public String showCreateDeviceForm(Model model, Authentication authentication) {

        DeviceDto device = new DeviceDto();
        model.addAttribute("device", device);


        Account account =
                    accountRepository.findByUsername(authentication.getName())
                            .orElseThrow(() -> new UserNotFoundException(authentication.getName()));
            model.addAttribute("currentUser", account);

            List<DeviceSubType> deviceSubTypes = deviceSubTypeService.findAll();
            model.addAttribute("subtypeList", deviceSubTypes);
            return "board/new-board";
    }

    @GetMapping("/front-assembly/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateFrontAssemblyForm(Model model, Authentication authentication) {
        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);
        model.addAttribute("boards", operationQueryService
                .findOperationsByStatusNames(Set.of(READY.getCode())).stream()
                .map(operation -> operation.getDevice())
                .filter(device -> device != null && !Boolean.TRUE.equals(device.getIsDeleted()))
                .distinct()
                .toList());
        model.addAttribute("frontAssemblyTypes", frontAssemblyTypeService.findAll());
        return "device/front-assembly-create";
    }

    @PostMapping("/front-assembly/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createFrontAssembly(@ModelAttribute FrontAssemblyCreateDto form,
                                      Authentication authentication,
                                      org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(authentication.getName()));
        try {
            frontAssemblyFlowService.create(form, account);
            redirectAttributes.addFlashAttribute("success", "Сборка создана и передана на ОТК №1");
            return "redirect:/device/front-assembly/otk";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/device/front-assembly/create";
        }
    }

    @GetMapping("/front-assembly/type")
    @PreAuthorize("hasRole('ADMIN')")
    public String showFrontAssemblyTypeForm(Model model, Authentication authentication) {
        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);
        model.addAttribute("subtypeList", deviceSubTypeService.findAll());
        model.addAttribute("frontAssemblyTypeList", frontAssemblyTypeService.findAll());
        model.addAttribute("frontAssemblyType", new FrontAssemblyTypeDto(null, null, null, null));
        return "device/front-assembly-type";
    }

    @PostMapping("/front-assembly/type")
    @PreAuthorize("hasRole('ADMIN')")
    public String createFrontAssemblyType(
            @Valid @ModelAttribute("frontAssemblyType") FrontAssemblyTypeDto form,
            BindingResult bindingResult,
            @RequestParam(value = "assemblyInstruction", required = false) MultipartFile assemblyInstruction,
            @RequestParam(value = "assemblyDiagram", required = false) MultipartFile assemblyDiagram,
            Model model,
            Authentication authentication) {
        if (bindingResult.hasErrors()) {
            populateFrontAssemblyTypeModel(model, authentication);
            return "device/front-assembly-type";
        }

        try {
            frontAssemblyTypeService.save(form, assemblyInstruction, assemblyDiagram);
            return "redirect:/device/front-assembly/type?success";
        } catch (IOException | IllegalArgumentException e) {
            populateFrontAssemblyTypeModel(model, authentication);
            model.addAttribute("error", e.getMessage());
            return "device/front-assembly-type";
        }
    }

    private void populateFrontAssemblyTypeModel(Model model, Authentication authentication) {
        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(authentication.getName()));
        model.addAttribute("currentUser", account);
        model.addAttribute("subtypeList", deviceSubTypeService.findAll());
        model.addAttribute("frontAssemblyTypeList", frontAssemblyTypeService.findAll());
    }

    @PostMapping("/create-board")
    public String createDevice(@ModelAttribute("device") DeviceDto device,
                               Model model,
                               BindingResult result,
                               Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);

        // Проверки
        if (device.getSerialNumber() == null || device.getSerialNumber().trim().isEmpty()) {
            List<DeviceSubType> deviceSubTypes = deviceSubTypeService.findAll();
            model.addAttribute("subtypeList", deviceSubTypes);
            model.addAttribute("device", device);
            model.addAttribute("error", "Номер не может быть пустым");
            return "board/new-board";
        }

        if (deviceService.existsSerialNumber(device.getSerialNumber())) {
            List<DeviceSubType> deviceSubTypes = deviceSubTypeService.findAll();
            model.addAttribute("subtypeList", deviceSubTypes);
            model.addAttribute("device", device);
            model.addAttribute("error", "Номер должен быть уникальным");
            return "board/new-board";
        }

        if (result.hasErrors()) {
            List<DeviceSubType> deviceSubTypes = deviceSubTypeService.findAll();
            model.addAttribute("subtypeList", deviceSubTypes);
            model.addAttribute("device", device);
            return "board/new-board";
        }

        try {
            deviceService.createDevice(device, account.getUsername());
            linkService.createFile(device.getSerialNumber());
            return "redirect:/main";
        } catch (RuntimeException e) {
            model.addAttribute("device", device);
            model.addAttribute("error", e.getMessage());
            device.setSerialNumber(null);
            return "board/new-board";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @DeleteMapping("/delete-board/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
