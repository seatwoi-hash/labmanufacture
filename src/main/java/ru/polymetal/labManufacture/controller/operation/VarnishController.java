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
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_6;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_5_1_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.VARNISH;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/device")
public class VarnishController {

    private final AccountRepository accountRepository;
    private final OperationService operationService;
    private final DeviceStatusService deviceStatusService;

    public VarnishController(AccountRepository accountRepository, OperationService operationService,
                             DeviceStatusService deviceStatusService) {
        this.accountRepository = accountRepository;
        this.operationService = operationService;
        this.deviceStatusService = deviceStatusService;
    }

    @GetMapping("/varnish-board")
    public String showVarnishDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = new ArrayList<>();

        devices.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(QUALITY_CHECK_5).getId()
        ));

        devices.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(FAIL_QUALITY_CHECK_6).getId()
        ));

        devices.addAll(operationService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(QUALITY_CHECK_5_1_1).getId()
        ));

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "operation/varnish/varnish-board";
    }


    @PostMapping("/varnish-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeVarnish(@RequestParam UUID deviceId,
                                             @ModelAttribute("device") Operation device,
                                             Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        operationService.completeOperationWithoutDescription(deviceId, account, VARNISH);

        return ResponseEntity.ok().build();

    }

}
