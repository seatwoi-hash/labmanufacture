package ru.polymetal.labManufacture.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.polymetal.labManufacture.constant.DeviceStatusCodes;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_4;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_TEST;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.INSTALLATION;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_4;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.READY;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.SIDE1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.SIDE2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_1;

import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TEST;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.VARNISH;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.WASHING1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.WASHING2;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.dto.DeviceDto;
import ru.polymetal.labManufacture.service.DeviceService;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.DeviceTypeService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/device")
public class DeviceController {

    private final AccountRepository accountRepository;
    private final DeviceService deviceService;
    private final DeviceStatusService deviceStatusService;
    private final DeviceSubTypeService deviceSubTypeService;


    public DeviceController(AccountRepository accountRepository,
                            DeviceService deviceService,
                            DeviceStatusService deviceStatusService, DeviceSubTypeService deviceSubTypeService) {
        this.accountRepository = accountRepository;
        this.deviceService = deviceService;
        this.deviceStatusService = deviceStatusService;
        this.deviceSubTypeService = deviceSubTypeService;
    }


    @GetMapping("/create-board")
    public String showCreateDeviceForm(Model model, Authentication authentication) {

        DeviceDto device = new DeviceDto();
        model.addAttribute("device", device);

        try {
            Account account =
                    accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                            "Пользователь не найден"));
            model.addAttribute("currentUser", account);

            List<DeviceSubType> deviceSubTypes = deviceSubTypeService.findAll();
            model.addAttribute("subtypeList", deviceSubTypes);
            return "new-board";
        } catch (RuntimeException e) {
            throw new RuntimeException("What is this");
        }
    }

    @PostMapping("/create-board")
    public String createDevice(@ModelAttribute("device") DeviceDto device,
                               Model model,
                               BindingResult result,
                               Authentication authentication) {

        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        model.addAttribute("currentUser", account);

        // Проверки
        if (device.getSerialNumber() == null || device.getSerialNumber().trim().isEmpty()) {
            List<DeviceSubType> deviceSubTypes = deviceSubTypeService.findAll();
            model.addAttribute("subtypeList", deviceSubTypes);
            model.addAttribute("device", device);
            model.addAttribute("error", "Номер не может быть пустым");
            return "new-board"; // currentUser уже в модели
        }

        if (deviceService.existsSerialNumber(device.getSerialNumber())) {
            List<DeviceSubType> deviceSubTypes = deviceSubTypeService.findAll();
            model.addAttribute("subtypeList", deviceSubTypes);
            model.addAttribute("device", device);
            model.addAttribute("error", "Номер должен быть уникальным");
            return "new-board"; // currentUser уже в модели
        }

        if (result.hasErrors()) {
            List<DeviceSubType> deviceSubTypes = deviceSubTypeService.findAll();
            model.addAttribute("subtypeList", deviceSubTypes);
            model.addAttribute("device", device);
            return "new-board"; // currentUser уже в модели
        }

        try {
            deviceService.createDevice(device, account.getUsername());
            return "redirect:/main";
        } catch (RuntimeException e) {
            model.addAttribute("device", device);
            model.addAttribute("error", e.getMessage());
            device.setSerialNumber(null);
            return "new-board"; // currentUser уже в модели
        }
    }

    @GetMapping("/mone-board")
    public String showMOneDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = deviceService.findByStatusIdAndIsDelete(deviceStatusService
                .findByName(DeviceStatusCodes.CREATE).getId());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "mone-board";
    }

    @PostMapping("/mone-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeMOne(@RequestParam UUID deviceId,
                                          Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        deviceService.completeMOne(deviceId, account);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/mtwo-board")
    public String showMTwoDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = deviceService.findByStatusIdAndIsDelete(deviceStatusService
                .findByName(SIDE1).getId());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "mtwo-board";
    }

    @PostMapping("/mtwo-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeMTwo(@RequestParam UUID deviceId,
                                          Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        deviceService.completeMTwo(deviceId, account);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/otk1-board")
    public String showOtkOneDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = Stream.concat(
                deviceService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(SIDE2).getId()
                ).stream(),
                deviceService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(REPAIR1).getId()
                ).stream()
        ).collect(Collectors.toList());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "otk1-board";
    }

    @PostMapping("/otk1-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeOtkOne(@RequestParam UUID deviceId,
                                            @RequestParam String action,
                                            @ModelAttribute("device") Device device,
                                            Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));
        if ("passed".equals(action)) {
            deviceService.completeOTKOne(deviceId, account, device.getDescription());
        } else if ("failed".equals(action)) {
            deviceService.failOTKOne(deviceId, account, device.getDescription());
        }

        return ResponseEntity.ok().build();

    }

    @GetMapping("/repair1-board")
    public String showRepairOneDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = deviceService.findByStatusIdAndIsDelete(deviceStatusService
                .findByName(FAIL_QUALITY_CHECK_1).getId());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "repair1-board";
    }


    @PostMapping("/repair1-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeRepairOne(@RequestParam UUID deviceId,
                                               @ModelAttribute("device") Device device,
                                               Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        deviceService.completeOperationWithDescription(deviceId, account, REPAIR1, device.getDescription());


        return ResponseEntity.ok().build();

    }

    @GetMapping("/installation-board")
    public String showInstallationDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = deviceService.findByStatusIdAndIsDelete(deviceStatusService
                .findByName(QUALITY_CHECK_1).getId());

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
                                                  @ModelAttribute("device") Device device,
                                                  Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        deviceService.completeOperationWithDescription(deviceId, account, INSTALLATION, device.getDescription());


        return ResponseEntity.ok().build();

    }

    @GetMapping("/otk2-board")
    public String showOtkTwoDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = Stream.concat(
                deviceService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(INSTALLATION).getId()
                ).stream(),
                deviceService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(REPAIR2).getId()
                ).stream()
        ).collect(Collectors.toList());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "otk2-board";
    }

    @PostMapping("/otk2-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeOtkTwo(@RequestParam UUID deviceId,
                                            @RequestParam String action,
                                            @ModelAttribute("device") Device device,
                                            Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));
        if ("passed".equals(action)) {
            deviceService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_2, device.getDescription());
        } else if ("failed".equals(action)) {
            deviceService.completeOperationWithDescription(deviceId, account, FAIL_QUALITY_CHECK_2, device.getDescription());
        }

        return ResponseEntity.ok().build();

    }

    @GetMapping("/repair2-board")
    public String showRepairTwoDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = deviceService.findByStatusIdAndIsDelete(deviceStatusService
                .findByName(FAIL_QUALITY_CHECK_2).getId());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "repair2-board";
    }


    @PostMapping("/repair2-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeRepairTwo(@RequestParam UUID deviceId,
                                               @ModelAttribute("device") Device device,
                                               Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        deviceService.completeOperationWithDescription(deviceId, account, REPAIR2, device.getDescription());


        return ResponseEntity.ok().build();

    }

    @GetMapping("/washing1-board")
    public String showWashingOneDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = Stream.concat(
                deviceService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(QUALITY_CHECK_2).getId()
                ).stream(),
                deviceService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_QUALITY_CHECK_3).getId()
                ).stream()
        ).collect(Collectors.toList());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "washing1-board";
    }


    @PostMapping("/washing1-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeWashingOne(@RequestParam UUID deviceId,
                                                @ModelAttribute("device") Device device,
                                                Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        deviceService.completeOperationWithDescription(deviceId, account, WASHING1 ,device.getDescription());

        return ResponseEntity.ok().build();

    }

    @GetMapping("/otk3-board")
    public String showOtkThreeDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = deviceService.findByStatusIdAndIsDelete(deviceStatusService
                .findByName(WASHING1).getId());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "otk3-board";
    }

    @PostMapping("/otk3-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeOtkThree(@RequestParam UUID deviceId,
                                              @RequestParam String action,
                                              @ModelAttribute("device") Device device,
                                              Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));
        if ("passed".equals(action)) {
            deviceService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_3, device.getDescription());
        } else if ("failed".equals(action)) {
            deviceService.completeOperationWithDescription(deviceId, account, FAIL_QUALITY_CHECK_3, device.getDescription());
        }

        return ResponseEntity.ok().build();

    }

    @GetMapping("/repair3-board")
    public String showRepairThreeDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = Stream.concat(
                deviceService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_TEST).getId()
                ).stream(),
                deviceService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_QUALITY_CHECK_4).getId()
                ).stream()
        ).collect(Collectors.toList());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "repair3-board";
    }


    @PostMapping("/repair3-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeRepairThree(@RequestParam UUID deviceId,
                                                 @ModelAttribute("device") Device device,
                                                 Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        deviceService.completeOperationWithDescription(deviceId, account, REPAIR3, device.getDescription());


        return ResponseEntity.ok().build();

    }


    @GetMapping("/test-board")
    public String showTestDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = Stream.concat(
                deviceService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(QUALITY_CHECK_3).getId()
                ).stream(),
                deviceService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(QUALITY_CHECK_4).getId()
                ).stream()
        ).collect(Collectors.toList());
        model.addAttribute("devices", devices);

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
                                          @ModelAttribute("device") Device device,
                                          Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));
        if ("passed".equals(action)) {
            deviceService.completeOperationWithDescription(deviceId, account, TEST, device.getDescription());
        } else if ("failed".equals(action)) {
            deviceService.completeOperationWithDescription(deviceId, account, FAIL_TEST, device.getDescription());
        }

        return ResponseEntity.ok().build();

    }


    @GetMapping("/otk4-board")
    public String showOtkFourDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = deviceService.findByStatusIdAndIsDelete(deviceStatusService
                .findByName(REPAIR3).getId());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "otk4-board";
    }

    @PostMapping("/otk4-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeOtkFour(@RequestParam UUID deviceId,
                                             @RequestParam String action,
                                             @ModelAttribute("device") Device device,
                                             Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));
        if ("passed".equals(action)) {
            deviceService.completeOperationWithDescription(deviceId, account, QUALITY_CHECK_4 ,device.getDescription());
        } else if ("failed".equals(action)) {
            deviceService.completeOperationWithDescription(deviceId, account,FAIL_TEST,device.getDescription());
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/washing2-board")
    public String showWashingTwoDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = deviceService.findByStatusIdAndIsDelete(deviceStatusService
                .findByName(TEST).getId());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "washing2-board";
    }


    @PostMapping("/washing2-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeWashingTwo(@RequestParam UUID deviceId,
                                                @ModelAttribute("device") Device device,
                                                Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        deviceService.completeOperationWithDescription(deviceId, account, WASHING2, device.getDescription());

        return ResponseEntity.ok().build();

    }

    @GetMapping("/varnish-board")
    public String showVarnishDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = Stream.concat(
                deviceService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(WASHING2).getId()
                ).stream(),
                deviceService.findByStatusIdAndIsDelete(
                        deviceStatusService.findByName(FAIL_QUALITY_CHECK_5).getId()
                ).stream()
        ).collect(Collectors.toList());

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "varnish-board";
    }


    @PostMapping("/varnish-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeVarnish(@RequestParam UUID deviceId,
                                             @ModelAttribute("device") Device device,
                                             Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        deviceService.completeOperationWithoutDescription(deviceId, account, VARNISH);

        return ResponseEntity.ok().build();

    }

    @GetMapping("/otk5-board")
    public String showOtkFiveDeviceForm(Model model, Authentication authentication) {

        List<Device> devices = deviceService.findByStatusIdAndIsDelete(deviceStatusService
                .findByName(VARNISH).getId());


        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "otk5-board";
    }

    @PostMapping("/otk5-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeOtkFive(@RequestParam UUID deviceId,
                                             @RequestParam String action,
                                             @ModelAttribute("device") Device device,
                                             Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));
        if ("passed".equals(action)) {
            deviceService.completeOperationWithDescription(deviceId, account,QUALITY_CHECK_5, device.getDescription());
            deviceService.completeOperationWithDescription(deviceId, account, READY, device.getDescription());
        } else if ("failed".equals(action)) {
            deviceService.completeOperationWithDescription(deviceId, account, FAIL_QUALITY_CHECK_5, device.getDescription());
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/ready-board")
    public String showReadyBoard(Model model, Authentication authentication) {

        // Находим устройства со статусом "Готовые"
        List<Device> devices = deviceService.findByStatusIdAndIsDelete(
                deviceStatusService.findByName(READY).getId()
        );

        // Объединяем списки
        List<Device> sortDevices = new ArrayList<>();

        // Сортируем по дате завершения (если есть) или дате создания
        sortDevices =
                devices.stream().sorted(Comparator.comparing(Device::getCreatedTime).reversed()).collect(Collectors.toList());

        model.addAttribute("devices", sortDevices);

        // Информация о текущем пользователе
        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "ready-board";
    }

    @GetMapping("/noready-board")
    public String showNoReadyBoard(Model model, Authentication authentication) {

        List<Device> devices = deviceService.findAll().stream().filter(a -> !a.getIsDeleted())
                .filter(a -> !a.getStatus().getName().equals(READY))
                .filter(a->a.getType().getName().equals("BOARD"))
                .sorted(Comparator.comparing(Device::getCreatedTime).reversed()).toList();


        model.addAttribute("devices", devices);

        // Информация о текущем пользователе
        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "noready-board";
    }

    @GetMapping("/operation-board/{sn}")
    public String showOperationBoard(Model model, Authentication authentication, @PathVariable String sn) {

        // Находим устройства со статусом "Готовые"
        List<Device> operations = deviceService.findBySerialNumberContainingIgnoreCase(sn);


        model.addAttribute("operations", operations);

        // Информация о текущем пользователе
        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        model.addAttribute("currentUser", account);

        return "all-operation-board";
    }

}
