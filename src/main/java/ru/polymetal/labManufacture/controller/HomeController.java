package ru.polymetal.labManufacture.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.READY;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.dto.DeviceDto;
import ru.polymetal.labManufacture.service.AccountService;
import ru.polymetal.labManufacture.service.DeviceService;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final DeviceService deviceService;

    public HomeController(AccountRepository accountRepository, AccountService accountService, DeviceService deviceService) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.deviceService = deviceService;
    }

    @GetMapping("/main")
    public String showHomePage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }
        int boardsProducedToday = deviceService.getBoardsProducedToday();
        model.addAttribute("boardsProducedToday", boardsProducedToday);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        List<Device> devices = deviceService.findDevicesForRole(account).stream()
                .filter(a -> !a.getIsDeleted())
                .filter(a -> a.getType().getName().equals("BOARD"))
                .collect(Collectors.groupingBy(Device::getSerialNumber))
                .entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .noneMatch(device -> device.getStatus().getName().equals(QUALITY_CHECK_5)
                                || device.getStatus().getName().equals(READY)))
                .flatMap(entry -> entry.getValue().stream())
                .sorted(Comparator.comparing(Device::getCreatedTime).reversed())
                .toList();

        List<DeviceDto> deviceDtos= devices.stream()
                .map(device -> {
                    DeviceDto dto = new DeviceDto();
                    dto.setSerialNumber(device.getSerialNumber());
                    dto.setSubType(device.getSubType());
                    dto.setStatus(deviceService.getNextStatus(device.getStatus()));
                    dto.setCreatedTime(device.getCreatedTime());
                    dto.setDescription(device.getDescription());
                    return dto;
                })
                .sorted(Comparator.comparing(DeviceDto::getCreatedTime).reversed())
                .toList();


        if (account != null) {
            model.addAttribute("currentUser", account);
        }

        model.addAttribute("devices", deviceDtos);

        return "main";
    }

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/main";
    }

    @GetMapping("adduser/check-username")
    public ResponseEntity<?> checkUsername(
            @RequestParam String username) {

        boolean exists = accountService.existsByUsername(username);

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", !exists);

        return ResponseEntity.ok(response);
    }

    @GetMapping("adduser/check-email")
    public ResponseEntity<?> checkEmail(
            @RequestParam String email) {

        boolean exists = accountService.existsByEmail(email);

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", !exists);

        return ResponseEntity.ok(response);
    }

}
