package ru.polymetal.labManufacture.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_6;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.READY;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.dto.DeviceDto;
import ru.polymetal.labManufacture.service.account.AccountService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final OperationService operationService;

    public HomeController(AccountRepository accountRepository, AccountService accountService,
                          OperationService operationService) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.operationService = operationService;
    }

    @GetMapping("/main")
    public String showHomePage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }

        int boardsProducedToday = operationService.getBoardsProducedToday();
        model.addAttribute("boardsProducedToday", boardsProducedToday);

        Optional<Account> accountOpt = accountRepository.findByUsername(authentication.getName());
        if (accountOpt.isEmpty()) {
            return "redirect:/login";
        }

        Account account = accountOpt.get();

        List<Operation> operations = operationService.findDevicesForRole(account).stream()
                .filter(a -> !a.getIsDeleted())
                .filter(operation -> operation.getDevice() != null
                        && "BOARD".equals(operation.getDevice().getType().getName()))
                .collect(Collectors.groupingBy(operation -> operation.getDevice().getSerialNumber()))
                .entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .noneMatch(device -> device.getStatus().getName().equals(QUALITY_CHECK_6)
                                || device.getStatus().getName().equals(READY)))
                .flatMap(entry -> entry.getValue().stream())
                .sorted(Comparator.comparing(Operation::getCreatedTime).reversed())
                .toList();

        List<DeviceDto> deviceDtos = operations.stream()
                .map(operation -> {
                    DeviceDto dto = new DeviceDto();
                    dto.setSerialNumber(operation.getDevice().getSerialNumber());
                    dto.setSubType(operation.getDevice().getSubtype());
                    dto.setStatus(operationService.getNextStatus(operation.getStatus()));
                    dto.setCreatedTime(operation.getDevice().getCreatedTime());
                    dto.setDescription(operation.getDescription());
                    return dto;

                })
                .sorted(Comparator.comparing(DeviceDto::getCreatedTime).reversed())
                .toList();


        model.addAttribute("currentUser", account);


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
