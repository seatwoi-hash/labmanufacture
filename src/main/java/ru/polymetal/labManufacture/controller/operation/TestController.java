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
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_TEST;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_TEST_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_2_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_4;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_4_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_4_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TEST;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TEST_2;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.exception.OperationNotFoundException;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.file.FileService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/device")
public class TestController {

    private final AccountRepository accountRepository;
    private final OperationService operationService;
    private final DeviceStatusService deviceStatusService;
    public final FileService fileService;
    private final DeviceSubTypeService deviceSubTypeService;



    public TestController(AccountRepository accountRepository, OperationService operationService,
                          DeviceStatusService deviceStatusService, FileService fileService, DeviceSubTypeService deviceSubTypeService) {
        this.accountRepository = accountRepository;
        this.operationService = operationService;
        this.deviceStatusService = deviceStatusService;
        this.fileService = fileService;
        this.deviceSubTypeService = deviceSubTypeService;
    }

    @GetMapping("/test-board")
    public String showTestDeviceForm(Model model, Authentication authentication) {

        List<Operation> operations = new ArrayList<>();

        operations.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(QUALITY_CHECK_2).getId()
        ));

        operations.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(QUALITY_CHECK_2_1).getId()
        ));

        operations.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(QUALITY_CHECK_3).getId()
        ));

        operations.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(TECHNICAL).getId()
        ));


        model.addAttribute("devices", operations);
        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);


        return "operation/test/test-board";
    }

    @PostMapping("/test-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeTest(@RequestParam UUID deviceId,
                                          @RequestParam String action,
                                          @ModelAttribute("device") Operation device,
                                          Authentication authentication) throws IOException {
        UUID operationIdTech = null;

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        Operation operation = operationService.findById(deviceId).orElseThrow(OperationNotFoundException::new);
        Boolean isTestOne = deviceSubTypeService.findIsTestTwoById(operation);

        if ("passed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, TEST,
                    device.getDescription());
        } else if ("failed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, FAIL_TEST,
                    device.getDescription());
        }

        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(OperationNotFoundException::new);

        if (!isTestOne && !"failed".equals(action)) {
            operationIdTech =  operationService.completeOperationWithoutDescription(operationIdTech, account,
                    TECHNICAL2);
        }

        return ResponseEntity.ok().build();

    }

    @GetMapping("/test-board-two")
    public String showTestTwoDeviceForm(Model model, Authentication authentication) {

        List<Operation> operations = new ArrayList<>();

        operations.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(QUALITY_CHECK_4).getId()
        ));

        operations.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(QUALITY_CHECK_4_1).getId()
        ));

        operations.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(QUALITY_CHECK_4_2).getId()
        ));
        model.addAttribute("devices", operations);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "operation/test/test2-board";
    }

    @PostMapping("/test-board-two/complete")
    @ResponseBody
    public ResponseEntity<?> completeTestTwo(@RequestParam UUID deviceId,
                                             @RequestParam String action,
                                             @ModelAttribute("device") Operation device,
                                             Authentication authentication) {
        UUID operationIdTech = null;

        Operation operation = operationService.findById(deviceId).orElseThrow(OperationNotFoundException::new);

        Boolean isTestTwoById = deviceSubTypeService.findIsTestTwoById(operation);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        if ("passed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, TEST_2,
                    device.getDescription());
        } else if ("failed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, FAIL_TEST_2,
                    device.getDescription());
        }

        if(!isTestTwoById) {
            operationService.completeOperationWithoutDescription(operationIdTech, account, TECHNICAL2);
        }

        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(OperationNotFoundException::new);


        return ResponseEntity.ok().build();

    }

}
