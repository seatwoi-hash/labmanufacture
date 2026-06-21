package ru.polymetal.labManufacture.controller.operation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_5_1_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_5_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TEST_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.WASHING1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.WASHING2;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/device")
public class WashingController {

    private final AccountRepository accountRepository;
    private final OperationService operationService;
    private final DeviceStatusService deviceStatusService;

    public WashingController(AccountRepository accountRepository, OperationService operationService,
                             DeviceStatusService deviceStatusService) {
        this.accountRepository = accountRepository;
        this.operationService = operationService;
        this.deviceStatusService = deviceStatusService;
    }
    @GetMapping("/washing1-board")
    public String showWashingOneDeviceForm(Model model, Authentication authentication) {



        List<Operation> operations = new ArrayList<>();

        operations.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(TEST_2).getId()
        ));

        operations.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(TECHNICAL2).getId()
        ));

        model.addAttribute("devices", operations);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "operation/washing/washing1-board";
    }


    @PostMapping("/washing1-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeWashingOne(@RequestParam UUID deviceId,
                                                @ModelAttribute("device") Operation device,
                                                Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        operationService.completeOperationWithDescription(deviceId, account, WASHING1 ,device.getDescription());

        return ResponseEntity.ok().build();

    }

    @GetMapping("/washing2-board")
    public String showWashingTwoDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = Stream.concat(
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(QUALITY_CHECK_5_1).getId()
                ).stream(),
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_QUALITY_CHECK_5_1_1).getId()
                ).stream()
        ).collect(Collectors.toList());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "operation/washing/washing2-board";
    }


    @PostMapping("/washing2-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeWashingTwo(@RequestParam UUID deviceId,
                                                @ModelAttribute("device") Operation device,
                                                Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        operationService.completeOperationWithDescription(deviceId, account, WASHING2, device.getDescription());

        return ResponseEntity.ok().build();

    }
}
