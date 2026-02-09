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
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_5_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.INSTALLATION;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.INSTALLATION2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_1_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TEST;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.OperationService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/device")
public class InstallationController {

    private final AccountRepository accountRepository;
    private final OperationService operationService;
    private final DeviceStatusService deviceStatusService;
    private final DeviceSubTypeService deviceSubTypeService;


    public InstallationController(AccountRepository accountRepository, OperationService operationService,
                                  DeviceStatusService deviceStatusService, DeviceSubTypeService deviceSubTypeService) {
        this.accountRepository = accountRepository;
        this.operationService = operationService;
        this.deviceStatusService = deviceStatusService;
        this.deviceSubTypeService = deviceSubTypeService;
    }

    @GetMapping("/installation-board")
    public String showInstallationDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = Stream.concat(
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(QUALITY_CHECK_1).getId()
                ).stream(),
                operationService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(QUALITY_CHECK_1_1).getId()
                ).stream()
        ).collect(Collectors.toList());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "installation-board";
    }


    @PostMapping("/installation-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeInstallation(@RequestParam UUID deviceId,
                                                  @ModelAttribute("device") Operation device,
                                                  Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        operationService.completeOperationWithDescription(deviceId, account, INSTALLATION, device.getDescription());


        return ResponseEntity.ok().build();

    }

    @GetMapping("/installation-board-two")
    public String showInstallationTwoDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = operationService.findByStatusIdAndIsDelete(deviceStatusService
                .findByName(TEST).getId());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "installation-board2";
    }


    @PostMapping("/installation-board-two/complete")
    @ResponseBody
    public ResponseEntity<?> completeInstallationTwo(@RequestParam UUID deviceId,
                                                     @ModelAttribute("device") Operation device,
                                                     Authentication authentication) {


        Operation operation = operationService.findById(deviceId).orElseThrow(() -> new RuntimeException("Операция не" +
                " найдена"));
        Boolean isTestTwoById = deviceSubTypeService.findIsTestTwoById(operation);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        UUID operationIdTech = operationService.completeOperationWithDescription(deviceId, account, INSTALLATION2, device.getDescription());


        if(!isTestTwoById) {
            operationService.completeOperationWithoutDescription(operationIdTech, account, TECHNICAL2);
        }

        return ResponseEntity.ok().build();

    }

}
