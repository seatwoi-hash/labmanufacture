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
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.DIAGNOSTICIAN_REPAIR_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.DIAGNOSTICIAN_TEST_2;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.exception.OperationNotFoundException;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.device.DeviceService;
import ru.polymetal.labManufacture.service.operation.OperationQueryService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.DIAGNOSTICIAN_REPAIR_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.DIAGNOSTICIAN_TEST_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_TEST;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_TEST_2;

@Controller
@RequestMapping("/device")
public class DiagnosticianController {

    private final AccountRepository accountRepository;
    private final OperationService operationService;
    private final DeviceStatusService deviceStatusService;
    private final DeviceSubTypeService deviceSubTypeService;
    private final DeviceService deviceService;
    private final OperationQueryService operationQueryService;


    public DiagnosticianController(AccountRepository accountRepository, OperationService operationService,
                                   DeviceStatusService deviceStatusService, DeviceSubTypeService deviceSubTypeService
            , DeviceService deviceService, OperationQueryService operationQueryService) {
        this.accountRepository = accountRepository;
        this.operationService = operationService;
        this.deviceStatusService = deviceStatusService;
        this.deviceSubTypeService = deviceSubTypeService;
        this.deviceService = deviceService;
        this.operationQueryService = operationQueryService;
    }

    @GetMapping("/diagnostician-one")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIAGNOSTICIAN')")
    public String showDiagnosticianOne(Model model, Authentication authentication) {

        List<Operation> devices = operationQueryService
                .findOperationsByStatusNames(Set.of(FAIL_TEST.getCode()));

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);

        return "operation/diagnostician/diagnostician-one";
    }

    @GetMapping("/diagnostician-two")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIAGNOSTICIAN')")
    public String showDiagnosticianTwo(Model model, Authentication authentication) {

        List<Operation> devices = operationQueryService
                .findOperationsByStatusNames(Set.of(FAIL_TEST_2.getCode()));

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);

        return "operation/diagnostician/diagnostician-two";
    }

    @PostMapping("/diagnostician-one/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIAGNOSTICIAN')")
    @ResponseBody
    public ResponseEntity<?> completeDiagnosticianOne(@RequestParam UUID deviceId,
                                            @RequestParam String action,
                                            @ModelAttribute("operation") Operation oldOperation,
                                            Authentication authentication) throws IOException {

        UUID operationIdTech = null;

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));
        Operation operation = operationService.findById(deviceId).orElseThrow(OperationNotFoundException::new);

        if ("repair".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                    DIAGNOSTICIAN_REPAIR_1.getCode(),
                    oldOperation.getDescription());
        }

        if ("test".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                    DIAGNOSTICIAN_TEST_1.getCode(),
                    oldOperation.getDescription());
        }

        Operation operationNew =
                operationService.findById(operationIdTech).orElseThrow(OperationNotFoundException::new);


        return ResponseEntity.ok().build();

    }

    @PostMapping("/diagnostician-two/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIAGNOSTICIAN')")
    @ResponseBody
    public ResponseEntity<?> completeDiagnosticianTwo(@RequestParam UUID deviceId,
                                            @RequestParam String action,
                                            @ModelAttribute("operation") Operation oldOperation,
                                            Authentication authentication) throws IOException {

        UUID operationIdTech = null;

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));
        Operation operation = operationService.findById(deviceId).orElseThrow(OperationNotFoundException::new);

        if ("repair".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                    DIAGNOSTICIAN_REPAIR_2.getCode(),
                    oldOperation.getDescription());
        }

        if ("test".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                    DIAGNOSTICIAN_TEST_2.getCode(),
                    oldOperation.getDescription());
        }

        Operation operationNew =
                operationService.findById(operationIdTech).orElseThrow(OperationNotFoundException::new);


        return ResponseEntity.ok().build();

    }

}
