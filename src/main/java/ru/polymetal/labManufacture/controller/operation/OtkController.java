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
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/device")
public class OtkController {

    private final AccountRepository accountRepository;
    private final OperationService operationService;
    private final DeviceStatusService deviceStatusService;
    private final DeviceSubTypeService deviceSubTypeService;
    public final FileService fileService;

    public OtkController(AccountRepository accountRepository, OperationService operationService,
                         DeviceStatusService deviceStatusService, DeviceSubTypeService deviceSubTypeService, FileService fileService) {
        this.accountRepository = accountRepository;
        this.operationService = operationService;
        this.deviceStatusService = deviceStatusService;
        this.deviceSubTypeService = deviceSubTypeService;
        this.fileService = fileService;
    }

    @GetMapping("/otk1-board")
    public String showOtkOneDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = new ArrayList<>();

        devices.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(SIDE2).getId()
        ));

        devices.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(REPAIR1).getId()
        ));

        devices.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(TECHNICAL3).getId()
        ));



        model.addAttribute("devices", devices);
        model.addAttribute("nextStatus", operationService.getNEXT_STATUS_MAPPING());


        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "otk1-board";
    }

    @PostMapping("/otk1-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeOtkOne(@RequestParam UUID deviceId,
                                            @RequestParam String action,
                                            @ModelAttribute("device") Operation device,
                                            Authentication authentication) throws IOException {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        Operation operation = operationService.findById(deviceId).orElseThrow(OperationNotFoundException::new);

        Boolean isInstallation = deviceSubTypeService.findIsInstallationOneById(operation);
        UUID operationIdTech = null;

        if ("passed".equals(action)) {
            if (operation.getStatus().getName().equals(SIDE2) || operation.getStatus().getName().equals(TECHNICAL3)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_1,
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR1)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        QUALITY_CHECK_1_1,
                        device.getDescription());
            }
        } else if ("failed".equals(action)) {
            if (operation.getStatus().getName().equals(SIDE2)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account, FAIL_QUALITY_CHECK_1,
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR1)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account, FAIL_QUALITY_CHECK_1_1,
                        device.getDescription());
            }
        }


        if (!isInstallation && !"failed".equals(action)) {
          operationIdTech =  operationService.completeOperationWithoutDescription(operationIdTech, account, TECHNICAL);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/otk2-board")
    public String showOtkTwoDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = Stream.concat(
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(INSTALLATION).getId()
                ).stream(),
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(REPAIR2).getId()
                ).stream()
        ).collect(Collectors.toList());

        model.addAttribute("devices", devices);
        model.addAttribute("nextStatus", operationService.getNEXT_STATUS_MAPPING());


        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "otk2-board";
    }

    @PostMapping("/otk2-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeOtkTwo(@RequestParam UUID deviceId,
                                            @RequestParam String action,
                                            @ModelAttribute("device") Operation device,
                                            Authentication authentication) throws IOException {

        UUID operationIdTech = null;

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));
        Operation operation = operationService.findById(deviceId).orElseThrow(OperationNotFoundException::new);

        if ("passed".equals(action)) {
            if (operation.getStatus().getName().equals(INSTALLATION)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_2,
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR2)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        QUALITY_CHECK_2_1,
                        device.getDescription());
            }
        } else if ("failed".equals(action)) {
            if (operation.getStatus().getName().equals(INSTALLATION)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_2,
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR2)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_2_1,
                        device.getDescription());
            }
        }

        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(OperationNotFoundException::new);


        return ResponseEntity.ok().build();

    }

    @GetMapping("/otk3-board")
    public String showOtkThreeDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = operationService.findByStatusIdAndIsDelete(deviceStatusService
                .findByName(REPAIR3).getId());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "otk3-board";
    }

    @PostMapping("/otk3-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeOtkThree(@RequestParam UUID deviceId,
                                              @RequestParam String action,
                                              @ModelAttribute("device") Operation device,
                                              Authentication authentication) throws IOException {
        UUID operationIdTech = null;

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        if ("passed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_3,
                    device.getDescription());
        } else if ("failed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, FAIL_QUALITY_CHECK_3,
                    device.getDescription());
        }

        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(OperationNotFoundException::new);


        return ResponseEntity.ok().build();

    }

    @GetMapping("/otk4-board")
    public String showOtkFourDeviceForm(Model model, Authentication authentication) {

        List<Operation> operations = new ArrayList<>();

        operations.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(INSTALLATION2).getId()
        ));

        operations.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(REPAIR4).getId()
        ));

        operations.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(REPAIR5).getId()
        ));

        model.addAttribute("devices", operations);
        model.addAttribute("nextStatus", operationService.getNEXT_STATUS_MAPPING());

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "otk4-board";
    }

    @PostMapping("/otk4-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeOtkFour(@RequestParam UUID deviceId,
                                             @RequestParam String action,
                                             @ModelAttribute("device") Operation device,
                                             Authentication authentication) throws IOException {
        UUID operationIdTech = null;

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        Operation operation = operationService.findById(deviceId).orElseThrow(OperationNotFoundException::new);

        if ("passed".equals(action)) {
            if (operation.getStatus().getName().equals(INSTALLATION2)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_4,
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR4)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        QUALITY_CHECK_4_1,
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR5)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        QUALITY_CHECK_4_2,
                        device.getDescription());
            }
        } else if ("failed".equals(action)) {
            if (operation.getStatus().getName().equals(INSTALLATION2)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_4,
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR4)) {
                operationIdTech =  operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_4_1,
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR5)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_4_2,
                        device.getDescription());
            }
        }

        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(OperationNotFoundException::new);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/otk5-board")
    public String showOtkFiveDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = new ArrayList<>();

        devices.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(WASHING1).getId()
        ));

        devices.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(REPAIR6).getId()
        ));

        devices.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(WASHING2).getId()
        ));


        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);
        model.addAttribute("nextStatus", operationService.getNEXT_STATUS_MAPPING());


        return "otk5-board";
    }

    @PostMapping("/otk5-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeOtkFive(@RequestParam UUID deviceId,
                                             @RequestParam String action,
                                             @ModelAttribute("device") Operation device,
                                             Authentication authentication) throws IOException {
        UUID operationIdTech = null;

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        Operation operation = operationService.findById(deviceId).orElseThrow(OperationNotFoundException::new);


        if ("passed".equals(action)) {
            if (operation.getStatus().getName().equals(WASHING1)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_5,
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR6)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        QUALITY_CHECK_5_1,
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(WASHING2)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        QUALITY_CHECK_5_1_1,
                        device.getDescription());
            }
        } else if ("failed_wash".equals(action)) {

            operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                    FAIL_QUALITY_CHECK_5_1_1,
                    device.getDescription());

        } else if ("failed_repair".equals(action)) {
            if (operation.getStatus().getName().equals(WASHING1)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_5,
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(WASHING2)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_5_1,
                        device.getDescription());
            } else if (operation.getStatus().getName().equals(REPAIR6)) {
                operationIdTech = operationService.completeOperationWithDescription(deviceId, account,
                        FAIL_QUALITY_CHECK_5_1,
                        device.getDescription());
            }
        }

        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(OperationNotFoundException::new);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/otk6-board")
    public String showOtkSixDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = operationService.findByStatusIdAndIsDelete(deviceStatusService
                .findByName(VARNISH).getId());


        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);

        return "otk6-board";
    }

    @PostMapping("/otk6-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeOtkSix(@RequestParam UUID deviceId,
                                            @RequestParam String action,
                                            @ModelAttribute("device") Operation device,
                                            Authentication authentication) throws IOException {
        UUID operationIdTech = null;


        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));



        if ("passed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_6,
                    device.getDescription());

            operationService.completeOperationWithDescription(operationIdTech, account, READY, device.getDescription());
        } else if ("failed".equals(action)) {
            operationIdTech = operationService.completeOperationWithDescription(deviceId, account, FAIL_QUALITY_CHECK_6,
                    device.getDescription());
        }

        Operation operationNew = operationService.findById(operationIdTech).orElseThrow(OperationNotFoundException::new);


        return ResponseEntity.ok().build();
    }

}
