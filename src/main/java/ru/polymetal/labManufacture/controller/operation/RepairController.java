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
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_1_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_2_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_4;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_4_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_4_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_5_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_TEST;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_TEST_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR4;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR6;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/device")
public class RepairController {

    private final AccountRepository accountRepository;
    private final OperationService operationService;
    private final DeviceStatusService deviceStatusService;

    public RepairController(AccountRepository accountRepository, OperationService operationService,
                            DeviceStatusService deviceStatusService) {
        this.accountRepository = accountRepository;
        this.operationService = operationService;
        this.deviceStatusService = deviceStatusService;
    }


    @GetMapping("/repair1-board")
    public String showRepairOneDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = Stream.concat(
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_QUALITY_CHECK_1).getId()
                ).stream(),
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_QUALITY_CHECK_1_1).getId()
                ).stream()
        ).collect(Collectors.toList());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "repair1-board";
    }


    @PostMapping("/repair1-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeRepairOne(@RequestParam UUID deviceId,
                                               @ModelAttribute("device") Operation device,
                                               Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        operationService.completeOperationWithDescription(deviceId, account, REPAIR1, device.getDescription());


        return ResponseEntity.ok().build();

    }

    @GetMapping("/repair2-board")
    public String showRepairTwoDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = Stream.concat(
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_QUALITY_CHECK_2).getId()
                ).stream(),
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_QUALITY_CHECK_2_1).getId()
                ).stream()
        ).collect(Collectors.toList());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "repair2-board";
    }


    @PostMapping("/repair2-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeRepairTwo(@RequestParam UUID deviceId,
                                               @ModelAttribute("device") Operation device,
                                               Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        operationService.completeOperationWithDescription(deviceId, account, REPAIR2, device.getDescription());


        return ResponseEntity.ok().build();

    }

    @GetMapping("/repair3-board")
    public String showRepairThreeDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = Stream.concat(
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_TEST).getId()
                ).stream(),
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_QUALITY_CHECK_3).getId()
                ).stream()
        ).collect(Collectors.toList());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "repair3-board";
    }


    @PostMapping("/repair3-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeRepairThree(@RequestParam UUID deviceId,
                                                 @ModelAttribute("device") Operation device,
                                                 Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        operationService.completeOperationWithDescription(deviceId, account, REPAIR3, device.getDescription());


        return ResponseEntity.ok().build();

    }

    @GetMapping("/repair4-board")
    public String showRepairFourDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = Stream.concat(
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_QUALITY_CHECK_4).getId()
                ).stream(),
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_QUALITY_CHECK_4_1).getId()
                ).stream()
        ).collect(Collectors.toList());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "repair4-board";
    }


    @PostMapping("/repair4-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeRepairFour(@RequestParam UUID deviceId,
                                                @ModelAttribute("device") Operation device,
                                                Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        operationService.completeOperationWithDescription(deviceId, account, REPAIR4, device.getDescription());


        return ResponseEntity.ok().build();

    }

    @GetMapping("/repair5-board")
    public String showRepairFiveDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = Stream.concat(
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_TEST_2).getId()
                ).stream(),
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_QUALITY_CHECK_4_2).getId()
                ).stream()
        ).collect(Collectors.toList());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "repair5-board";
    }


    @PostMapping("/repair5-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeRepairFive(@RequestParam UUID deviceId,
                                                @ModelAttribute("device") Operation device,
                                                Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        operationService.completeOperationWithDescription(deviceId, account, REPAIR5, device.getDescription());


        return ResponseEntity.ok().build();

    }

    @GetMapping("/repair6-board")
    public String showRepairSixDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = Stream.concat(
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_QUALITY_CHECK_5).getId()
                ).stream(),
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_QUALITY_CHECK_5_1).getId()
                ).stream()
        ).collect(Collectors.toList());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "repair6-board";
    }


    @PostMapping("/repair6-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeRepairSix(@RequestParam UUID deviceId,
                                               @ModelAttribute("device") Operation device,
                                               Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        operationService.completeOperationWithDescription(deviceId, account, REPAIR6, device.getDescription());


        return ResponseEntity.ok().build();

    }

}
