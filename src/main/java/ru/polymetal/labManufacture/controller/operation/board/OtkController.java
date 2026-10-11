package ru.polymetal.labManufacture.controller.operation.board;

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
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_5_1_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_6;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_TEST_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.INSTALLATION;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.INSTALLATION2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_1_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_2_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_4;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_4_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_4_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_5_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_5_1_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_6;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.READY;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR4;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR6;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.SIDE2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.VARNISH;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.WASHING1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.WASHING2;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.exception.OperationNotFoundException;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.file.FileService;
import ru.polymetal.labManufacture.service.operation.OperationQueryService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * MVC-контроллер OtkController.
 *
 * @author Tatarinov Anton
 */
@Controller
@RequestMapping("/device")
public class OtkController {

    private final AccountRepository accountRepository;
    private final OperationService operationService;
    private final DeviceSubTypeService deviceSubTypeService;
    public final FileService fileService;
    private final OperationQueryService operationQueryService;

    public OtkController(AccountRepository accountRepository, OperationService operationService,
                            DeviceSubTypeService deviceSubTypeService,
                         FileService fileService, OperationQueryService operationQueryService) {
        this.accountRepository = accountRepository;
        this.operationService = operationService;
        this.deviceSubTypeService = deviceSubTypeService;
        this.fileService = fileService;
        this.operationQueryService = operationQueryService;
    }

    @GetMapping("/otk1-board")
    public String showOtkOneDeviceForm(Model model, Authentication authentication) {
        return showBoard(
                model,
                authentication,
                Set.of(SIDE2.getCode(), REPAIR1.getCode(), TECHNICAL3.getCode()),
                true,
                "operation/otk/otk1-board");
    }

    @PostMapping("/otk1-board/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    @ResponseBody
    public ResponseEntity<?> completeOtkOne(@RequestParam UUID deviceId,
                                            @RequestParam String action,
                                            @ModelAttribute("device") Operation device,
                                            Authentication authentication) throws IOException {

        if (!isAllowedAction(action, "passed", "failed")) {
            return ResponseEntity.badRequest().build();
        }

        Account account = findAccount(authentication);
        Operation operation = findOperation(deviceId);

        Boolean isInstallation = deviceSubTypeService.findIsInstallationOneById(operation);
        UUID operationIdTech = null;

        if ("passed".equals(action)) {
            if (operation.getStatus().getName().equals(SIDE2.getCode()) || operation.getStatus().getName().equals(TECHNICAL3.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_1.getCode(),
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR1.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        QUALITY_CHECK_1_1.getCode(),
                        device.getDescription());
            }
        } else if ("failed".equals(action)) {
            if (operation.getStatus().getName().equals(SIDE2.getCode()) || operation.getStatus().getName().equals(TECHNICAL3.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_1.getCode(),
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR1.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_1_1.getCode(),
                        device.getDescription());
            }
        }


        if (!isInstallation && !"failed".equals(action)) {
            operationIdTech = operationService.completeOperationWithoutDescription(operationIdTech, account, TECHNICAL.getCode());
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/otk2-board")
    public String showOtkTwoDeviceForm(Model model, Authentication authentication) {
        return showBoard(
                model,
                authentication,
                Set.of(INSTALLATION.getCode(), REPAIR2.getCode()),
                true,
                "operation/otk/otk2-board");
    }

    @PostMapping("/otk2-board/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    @ResponseBody
    public ResponseEntity<?> completeOtkTwo(@RequestParam UUID deviceId,
                                            @RequestParam String action,
                                            @ModelAttribute("device") Operation device,
                                            Authentication authentication) throws IOException {

        if (!isAllowedAction(action, "passed", "failed")) {
            return ResponseEntity.badRequest().build();
        }

        UUID operationIdTech = null;

        Account account = findAccount(authentication);
        Operation operation = findOperation(deviceId);

        if ("passed".equals(action)) {
            if (operation.getStatus().getName().equals(INSTALLATION.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_2.getCode(),
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR2.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        QUALITY_CHECK_2_1.getCode(),
                        device.getDescription());
            }
        } else if ("failed".equals(action)) {
            if (operation.getStatus().getName().equals(INSTALLATION.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_2.getCode(),
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR2.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_2_1.getCode(),
                        device.getDescription());
            }
        }

        ensureOperationExists(operationIdTech);


        return ResponseEntity.ok().build();

    }

    @GetMapping("/otk3-board")
    public String showOtkThreeDeviceForm(Model model, Authentication authentication) {
        return showBoard(
                model,
                authentication,
                Set.of(REPAIR3.getCode()),
                false,
                "operation/otk/otk3-board");
    }

    @PostMapping("/otk3-board/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    @ResponseBody
    public ResponseEntity<?> completeOtkThree(@RequestParam UUID deviceId,
                                              @RequestParam String action,
                                              @ModelAttribute("device") Operation device,
                                              Authentication authentication) throws IOException {

        if (!isAllowedAction(action, "passed", "failed")) {
            return ResponseEntity.badRequest().build();
        }

        UUID operationIdTech = null;

        Account account = findAccount(authentication);


        if ("passed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_3.getCode(),
                    device.getDescription());
        } else if ("failed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, FAIL_QUALITY_CHECK_3.getCode(),
                    device.getDescription());
        }

        ensureOperationExists(operationIdTech);


        return ResponseEntity.ok().build();

    }

    @GetMapping("/otk4-board")
    public String showOtkFourDeviceForm(Model model, Authentication authentication) {
        return showBoard(
                model,
                authentication,
                Set.of(INSTALLATION2.getCode(), REPAIR4.getCode(), REPAIR5.getCode()),
                true,
                "operation/otk/otk4-board");
    }

    @PostMapping("/otk4-board/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    @ResponseBody
    public ResponseEntity<?> completeOtkFour(@RequestParam UUID deviceId,
                                             @RequestParam String action,
                                             @ModelAttribute("device") Operation device,
                                             Authentication authentication) throws IOException {

        if (!isAllowedAction(action, "passed", "failed")) {
            return ResponseEntity.badRequest().build();
        }

        UUID operationIdTech = null;

        Account account = findAccount(authentication);
        Operation operation = findOperation(deviceId);

        if ("passed".equals(action)) {
            if (operation.getStatus().getName().equals(INSTALLATION2.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_4.getCode(),
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR4.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        QUALITY_CHECK_4_1.getCode(),
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR5.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        QUALITY_CHECK_4_2.getCode(),
                        device.getDescription());
            }
        } else if ("failed".equals(action)) {
            if (operation.getStatus().getName().equals(INSTALLATION2.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_4.getCode(),
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR4.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_4_1.getCode(),
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR5.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_4_2.getCode(),
                        device.getDescription());
            }
        }

        ensureOperationExists(operationIdTech);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/otk5-board")
    public String showOtkFiveDeviceForm(Model model, Authentication authentication) {
        return showBoard(
                model,
                authentication,
                Set.of(WASHING1.getCode(), REPAIR6.getCode(), WASHING2.getCode()),
                true,
                "operation/otk/otk5-board");
    }

    @PostMapping("/otk5-board/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    @ResponseBody
    public ResponseEntity<?> completeOtkFive(@RequestParam UUID deviceId,
                                             @RequestParam String action,
                                             @ModelAttribute("device") Operation device,
                                             Authentication authentication) throws IOException {

        if (!isAllowedAction(action, "passed", "failed_wash", "failed_repair")) {
            return ResponseEntity.badRequest().build();
        }

        UUID operationIdTech = null;

        Account account = findAccount(authentication);
        Operation operation = findOperation(deviceId);


        if ("passed".equals(action)) {
            if (operation.getStatus().getName().equals(WASHING1.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_5.getCode(),
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR6.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        QUALITY_CHECK_5_1.getCode(),
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(WASHING2.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        QUALITY_CHECK_5_1_1.getCode(),
                        device.getDescription());
            }
        } else if ("failed_wash".equals(action)) {

            operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                    FAIL_QUALITY_CHECK_5_1_1.getCode(),
                    device.getDescription());

        } else if ("failed_repair".equals(action)) {
            if (operation.getStatus().getName().equals(WASHING1.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_5.getCode(),
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(WASHING2.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_5_1.getCode(),
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR6.getCode())) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_5_1.getCode(),
                        device.getDescription());
            }
        }

        ensureOperationExists(operationIdTech);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/otk6-board")
    public String showOtkSixDeviceForm(Model model, Authentication authentication) {
        return showBoard(
                model,
                authentication,
                Set.of(VARNISH.getCode()),
                false,
                "operation/otk/otk6-board");
    }

    @PostMapping("/otk6-board/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    @ResponseBody
    public ResponseEntity<?> completeOtkSix(@RequestParam UUID deviceId,
                                            @RequestParam String action,
                                            @ModelAttribute("device") Operation device,
                                            Authentication authentication) throws IOException {

        if (!isAllowedAction(action, "passed", "failed")) {
            return ResponseEntity.badRequest().build();
        }

        UUID operationIdTech = null;


        Account account = findAccount(authentication);


        if ("passed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_6.getCode(),
                    device.getDescription());

            operationService.completeOperationWithDescription(operationIdTech, account, READY.getCode(), device.getDescription());
        } else if ("failed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, FAIL_QUALITY_CHECK_6.getCode(),
                    device.getDescription());
        }

        ensureOperationExists(operationIdTech);


        return ResponseEntity.ok().build();
    }

    private boolean isAllowedAction(String action, String... allowedActions) {
        return Stream.of(allowedActions).anyMatch(allowedAction -> allowedAction.equals(action));
    }

    private String showBoard(Model model, Authentication authentication, Set<String> statusNames,
                             boolean includeNextStatus, String viewName) {
        List<Operation> devices = operationQueryService.findOperationsByStatusNames(statusNames);
        model.addAttribute("devices", devices);
        model.addAttribute("currentUser", findAccount(authentication));
        if (includeNextStatus) {
            model.addAttribute("nextStatus", operationService.getNextStatusMapping());
        }
        return viewName;
    }

    private Account findAccount(Authentication authentication) {
        String username = authentication.getName();
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private Operation findOperation(UUID operationId) {
        return operationService.findById(operationId)
                .orElseThrow(OperationNotFoundException::new);
    }

    private void ensureOperationExists(UUID operationId) {
        findOperation(operationId);
    }

}
