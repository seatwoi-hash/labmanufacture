package ru.polymetal.labManufacture.controller.operation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.DIAGNOSTICIAN_REPAIR_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.DIAGNOSTICIAN_REPAIR_2;
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
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.INSTALLATION2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR4;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR6;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.data.repository.OperationRepository;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.operation.OperationQueryService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MVC-контроллер RepairController.
 *
 * @author Tatarinov Anton
 */
@Controller
@RequestMapping("/device")
public class RepairController {

    private final AccountRepository accountRepository;
    private final OperationService operationService;
    private final OperationQueryService operationQueryService;


    public RepairController(AccountRepository accountRepository, OperationService operationService,
                             OperationQueryService operationQueryService) {
        this.accountRepository = accountRepository;
        this.operationService = operationService;
        this.operationQueryService = operationQueryService;
    }


    @GetMapping("/repair1-board")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPAIRMAN')")
    public String showRepairOneDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = operationQueryService
                .findOperationsByStatusNames(Set.of(FAIL_QUALITY_CHECK_1.getCode(), FAIL_QUALITY_CHECK_1_1.getCode()));

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);

        return "operation/repair/repair1-board";
    }


    @PostMapping("/repair1-board/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPAIRMAN')")
    @ResponseBody
    public ResponseEntity<?> completeRepairOne(@RequestParam UUID deviceId,
                                               @ModelAttribute("device") Operation device,
                                               Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        operationService.completeOperationWithDescription(deviceId, account, REPAIR1.getCode(), device.getDescription());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/repair2-board")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPAIRMAN')")
    public String showRepairTwoDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = operationQueryService
                .findOperationsByStatusNames(Set.of(FAIL_QUALITY_CHECK_2.getCode(), FAIL_QUALITY_CHECK_2_1.getCode()));

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);

        return "operation/repair/repair2-board";
    }


    @PostMapping("/repair2-board/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPAIRMAN')")
    @ResponseBody
    public ResponseEntity<?> completeRepairTwo(@RequestParam UUID deviceId,
                                               @ModelAttribute("device") Operation device,
                                               Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        operationService.completeOperationWithDescription(deviceId, account, REPAIR2.getCode(), device.getDescription());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/repair3-board")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPAIRMAN')")
    public String showRepairThreeDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = operationQueryService
                .findOperationsByStatusNames(Set.of(DIAGNOSTICIAN_REPAIR_1.getCode(), FAIL_QUALITY_CHECK_3.getCode()));

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);

        return "operation/repair/repair3-board";
    }


    @PostMapping("/repair3-board/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPAIRMAN')")
    @ResponseBody
    public ResponseEntity<?> completeRepairThree(@RequestParam UUID deviceId,
                                                 @ModelAttribute("device") Operation device,
                                                 Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        operationService.completeOperationWithDescription(deviceId, account, REPAIR3.getCode(), device.getDescription());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/repair4-board")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPAIRMAN')")
    public String showRepairFourDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = operationQueryService
                .findOperationsByStatusNames(Set.of(FAIL_QUALITY_CHECK_4.getCode(), FAIL_QUALITY_CHECK_4_1.getCode()));


        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);

        return "operation/repair/repair4-board";
    }


    @PostMapping("/repair4-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeRepairFour(@RequestParam UUID deviceId,
                                                @ModelAttribute("device") Operation device,
                                                Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        operationService.completeOperationWithDescription(deviceId, account, REPAIR4.getCode(), device.getDescription());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/repair5-board")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPAIRMAN')")
    public String showRepairFiveDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = operationQueryService
                .findOperationsByStatusNames(Set.of(DIAGNOSTICIAN_REPAIR_2.getCode(), FAIL_QUALITY_CHECK_4_2.getCode()));

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);

        return "operation/repair/repair5-board";
    }


    @PostMapping("/repair5-board/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPAIRMAN')")
    @ResponseBody
    public ResponseEntity<?> completeRepairFive(@RequestParam UUID deviceId,
                                                @ModelAttribute("device") Operation device,
                                                Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        operationService.completeOperationWithDescription(deviceId, account, REPAIR5.getCode(), device.getDescription());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/repair6-board")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPAIRMAN')")
    public String showRepairSixDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = operationQueryService
                .findOperationsByStatusNames(Set.of(FAIL_QUALITY_CHECK_5.getCode(), FAIL_QUALITY_CHECK_5_1.getCode()));

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);

        return "operation/repair/repair6-board";
    }

    @PostMapping("/repair6-board/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPAIRMAN')")
    @ResponseBody
    public ResponseEntity<?> completeRepairSix(@RequestParam UUID deviceId,
                                               @ModelAttribute("device") Operation device,
                                               Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        operationService.completeOperationWithDescription(deviceId, account, REPAIR6.getCode(), device.getDescription());

        return ResponseEntity.ok().build();
    }
}
