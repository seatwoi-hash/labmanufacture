package ru.polymetal.labManufacture.controller.device;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.dto.DeviceDto;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.device.DeviceService;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.nextcloud.LinkService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

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
    private final LinkService linkService;


    public DeviceController(AccountRepository accountRepository, DeviceService deviceService,
                            DeviceSubTypeService deviceSubTypeService, LinkService linkService) {
        this.accountRepository = accountRepository;
        this.deviceService = deviceService;
        this.deviceSubTypeService = deviceSubTypeService;
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
    public String deleteDevice(@PathVariable UUID id)
    {
        deviceService.delete(id);

        return "redirect:/device/noready-board";
    }
}
