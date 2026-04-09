package ru.polymetal.labManufacture.controller;

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
import org.springframework.web.multipart.MultipartFile;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_TEST;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_TEST_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_2_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_4;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_4_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_4_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR6;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TEST;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TEST_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.WASHING1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.WASHING2;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.dto.FileUploadRequestDto;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.FileService;
import ru.polymetal.labManufacture.service.OperationService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/device")
public class TestController {

    private final AccountRepository accountRepository;
    private final OperationService operationService;
    private final DeviceStatusService deviceStatusService;
    public final FileService fileService;


    public TestController(AccountRepository accountRepository, OperationService operationService,
                          DeviceStatusService deviceStatusService, FileService fileService) {
        this.accountRepository = accountRepository;
        this.operationService = operationService;
        this.deviceStatusService = deviceStatusService;
        this.fileService = fileService;
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


        return "test-board";
    }

    @PostMapping("/test-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeTest(@RequestParam UUID deviceId,
                                          @RequestParam String action,
                                          @ModelAttribute("device") Operation device,
                                          Authentication authentication) throws IOException {
        UUID operationIdTech = null;
        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));
        if ("passed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, TEST,
                    device.getDescription());
        } else if ("failed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, FAIL_TEST,
                    device.getDescription());
        }

        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(() -> new RuntimeException(
                "Операция не" +
                " найдена"));

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
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "test2-board";
    }

    @PostMapping("/test-board-two/complete")
    @ResponseBody
    public ResponseEntity<?> completeTestTwo(@RequestParam UUID deviceId,
                                             @RequestParam String action,
                                             @ModelAttribute("device") Operation device,
                                             Authentication authentication) throws IOException {
        UUID operationIdTech = null;
        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));
        if ("passed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, TEST_2,
                    device.getDescription());
        } else if ("failed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, FAIL_TEST_2,
                    device.getDescription());
        }

        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(() -> new RuntimeException(
                "Операция не" +
                " найдена"));


        return ResponseEntity.ok().build();

    }

}
