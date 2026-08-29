package ru.polymetal.labManufacture.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
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
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.READY;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR4;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR6;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.SIDE1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.SIDE2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.WASHING1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.WASHING2;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.file.FileService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/device")
public class RollbackOtkController {

    private final AccountRepository accountRepository;
    private final OperationService operationService;
    private final DeviceSubTypeService deviceSubTypeService;
    public final FileService fileService;

    public RollbackOtkController(AccountRepository accountRepository, OperationService operationService,
                        DeviceSubTypeService deviceSubTypeService, FileService fileService) {
        this.accountRepository = accountRepository;
        this.operationService = operationService;
        this.deviceSubTypeService = deviceSubTypeService;
        this.fileService = fileService;
    }

    @PostMapping("/otk1-board/rollback")
    @ResponseBody
    public ResponseEntity<?> completeOtkOne(@RequestParam UUID deviceId,
                                            @RequestParam String action,
                                            @ModelAttribute("device") Operation device,
                                            Authentication authentication) throws IOException {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        Operation operation = operationService.findById(deviceId).orElseThrow(() -> new RuntimeException("Операция не" +
                " найдена"));


        UUID operationIdTech = null;


        Boolean isSideTwo = deviceSubTypeService.findIsTestTwoById(operation);


        if(operation.getStatus().getName().equals(QUALITY_CHECK_1.getCode()) && isSideTwo) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, SIDE2.getCode(),
                    "Возвращён  - " + device.getDescription());
        } else if (operation.getStatus().getName().equals(QUALITY_CHECK_1.getCode()) && !isSideTwo) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, SIDE1.getCode(),
                    "Возвращён  - " + device.getDescription());
        }

        if(operation.getStatus().getName().equals(QUALITY_CHECK_1_1.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, REPAIR1.getCode(),
                    "Возвращён  - " + device.getDescription());
        }


        if(operation.getStatus().getName().equals(FAIL_QUALITY_CHECK_1.getCode()) && isSideTwo) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, SIDE2.getCode(),
                    "Возвращён  - " + device.getDescription());
        } else if (operation.getStatus().getName().equals(FAIL_QUALITY_CHECK_1.getCode()) && !isSideTwo) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, SIDE1.getCode(),
                    "Возвращён  - " + device.getDescription());
        }

        if(operation.getStatus().getName().equals(FAIL_QUALITY_CHECK_1_1.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, REPAIR1.getCode(),
                    "Возвращён  - " + device.getDescription());
        }

        if(operation.getStatus().getName().equals(TECHNICAL.getCode())  && isSideTwo) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, SIDE2.getCode(),
                    "Возвращён  - " + device.getDescription());
        } else if (operation.getStatus().getName().equals(TECHNICAL.getCode()) && !isSideTwo) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, SIDE1.getCode(),
                    "Возвращён  - " + device.getDescription());
        }

        if(operation.getStatus().getName().equals(TECHNICAL2.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, SIDE2.getCode(),
                    "Возвращён  - " + device.getDescription());
        } else if (operation.getStatus().getName().equals(TECHNICAL2.getCode()) && !isSideTwo) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, SIDE1.getCode(),
                    "Возвращён  - " + device.getDescription());
        }



        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(() -> new RuntimeException("Операция не" +
                " найдена"));


        return ResponseEntity.ok().build();
    }

    @PostMapping("/otk2-board/rollback")
    @ResponseBody
    public ResponseEntity<?> completeOtkTwo(@RequestParam UUID deviceId,
                                            @RequestParam String action,
                                            @ModelAttribute("device") Operation device,
                                            Authentication authentication) throws IOException {



        UUID operationIdTech = null;

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));
        Operation operation = operationService.findById(deviceId).orElseThrow(() -> new RuntimeException("Операция не" +
                " найдена"));


        if(operation.getStatus().getName().equals(QUALITY_CHECK_2.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, INSTALLATION.getCode(),
                    "Возвращён  - " + device.getDescription());
        }


        if(operation.getStatus().getName().equals(REPAIR2.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_2_1.getCode(),
                    "Возвращён  - " + device.getDescription());
        }


        if(operation.getStatus().getName().equals(FAIL_QUALITY_CHECK_2.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, INSTALLATION.getCode(),
                    "Возвращён  - " + device.getDescription());
        }


        if(operation.getStatus().getName().equals(FAIL_QUALITY_CHECK_2_1.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, REPAIR2.getCode(),
                    "Возвращён  - " + device.getDescription());
        }





        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(() -> new RuntimeException("Операция не" +
                " найдена"));


        return ResponseEntity.ok().build();

    }


    @PostMapping("/otk3-board/rollback")
    @ResponseBody
    public ResponseEntity<?> completeOtkThree(@RequestParam UUID deviceId,
                                              @RequestParam String action,
                                              @ModelAttribute("device") Operation device,
                                              Authentication authentication) throws IOException {
        UUID operationIdTech = null;

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));


        Operation operation = operationService.findById(deviceId).orElseThrow(() -> new RuntimeException("Операция не" +
                " найдена"));


        if(operation.getStatus().getName().equals(QUALITY_CHECK_3.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, REPAIR3.getCode(),
                    "Возвращён  - " + device.getDescription());
        }


        if(operation.getStatus().getName().equals(FAIL_QUALITY_CHECK_3.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, REPAIR3.getCode(),
                    "Возвращён  - " + device.getDescription());
        }


        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(() -> new RuntimeException("Операция не" +
                " найдена"));


        return ResponseEntity.ok().build();

    }

    @PostMapping("/otk4-board/rollback")
    @ResponseBody
    public ResponseEntity<?> completeOtkFour(@RequestParam UUID deviceId,
                                             @RequestParam String action,
                                             @ModelAttribute("device") Operation device,
                                             Authentication authentication) throws IOException {
        UUID operationIdTech = null;

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));
        Operation operation = operationService.findById(deviceId).orElseThrow(() -> new RuntimeException("Операция не" +
                " найдена"));


        if(operation.getStatus().getName().equals(QUALITY_CHECK_4.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, INSTALLATION2.getCode(),
                    "Возвращён  - " + device.getDescription());
        }

        if(operation.getStatus().getName().equals(QUALITY_CHECK_4_1.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, REPAIR4.getCode(),
                    "Возвращён  - " + device.getDescription());
        }

        if(operation.getStatus().getName().equals(QUALITY_CHECK_4_2.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, REPAIR5.getCode(),
                    "Возвращён  - " + device.getDescription());
        }

        if(operation.getStatus().getName().equals(FAIL_QUALITY_CHECK_4.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, INSTALLATION2.getCode(),
                    "Возвращён  - " + device.getDescription());
        }


        if(operation.getStatus().getName().equals(FAIL_QUALITY_CHECK_4_1.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, REPAIR4.getCode(),
                    "Возвращён  - " + device.getDescription());
        }

        if(operation.getStatus().getName().equals(FAIL_QUALITY_CHECK_4_2.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, REPAIR5.getCode(),
                    "Возвращён  - " + device.getDescription());
        }


        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(() -> new RuntimeException("Операция не" +
                " найдена"));

        return ResponseEntity.ok().build();
    }

    @PostMapping("/otk5-board/rollback")
    @ResponseBody
    public ResponseEntity<?> completeOtkFive(@RequestParam UUID deviceId,
                                             @RequestParam String action,
                                             @ModelAttribute("device") Operation device,
                                             Authentication authentication) throws IOException {
        UUID operationIdTech = null;

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        Operation operation = operationService.findById(deviceId).orElseThrow(() -> new RuntimeException("Операция не" +
                " найдена"));


        Boolean isInstallation = deviceSubTypeService.findIsInstallationOneById(operation);
        Boolean isTestTwo = deviceSubTypeService.findIsTestTwoById(operation);
        Boolean isSideOne = deviceSubTypeService.findIsTestTwoById(operation);



        if(operation.getStatus().getName().equals(QUALITY_CHECK_5.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, WASHING1.getCode(),
                    "Возвращён  - " + device.getDescription());
        }


        if(operation.getStatus().getName().equals(QUALITY_CHECK_5_1.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, REPAIR6.getCode(),
                    "Возвращён  - " + device.getDescription());
        }


        if(operation.getStatus().getName().equals(QUALITY_CHECK_5_1_1.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, WASHING2.getCode(),
                    "Возвращён  - " + device.getDescription());
        }

        if(operation.getStatus().getName().equals(FAIL_QUALITY_CHECK_5.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, WASHING1.getCode(),
                    "Возвращён  - " + device.getDescription());
        }


        if(operation.getStatus().getName().equals(FAIL_QUALITY_CHECK_5_1_1.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, WASHING2.getCode(),
                    "Возвращён  - " + device.getDescription());
        }


        if(operation.getStatus().getName().equals(FAIL_QUALITY_CHECK_5_1.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, REPAIR6.getCode(),
                    "Возвращён  - " + "Возвращён  - " + device.getDescription());
        }



        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(() -> new RuntimeException("Операция не" +
                " найдена"));

        return ResponseEntity.ok().build();
    }

    @PostMapping("/otk6-board/rollback")
    @ResponseBody
    public ResponseEntity<?> completeOtkSix(@RequestParam UUID deviceId,
                                            @RequestParam String action,
                                            @ModelAttribute("device") Operation device,
                                            Authentication authentication) throws IOException {
        UUID operationIdTech = null;


        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        Operation operation = operationService.findById(deviceId).orElseThrow(() -> new RuntimeException("Операция не" +
                " найдена"));


        if(operation.getStatus().getName().equals(READY.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, WASHING1.getCode(),
                    "Возвращён  - " + device.getDescription());
        }

        if(operation.getStatus().getName().equals(FAIL_QUALITY_CHECK_6.getCode())) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, WASHING1.getCode(),
                    "Возвращён  - " + device.getDescription());
        }

        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(() -> new RuntimeException("Операция не" +
                " найдена"));


        return ResponseEntity.ok().build();
    }

}
