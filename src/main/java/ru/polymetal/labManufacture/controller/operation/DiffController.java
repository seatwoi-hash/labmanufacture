package ru.polymetal.labManufacture.controller.operation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.CREATE;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.READY;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.SIDE1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.SIDE2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL3;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.exception.OperationNotFoundException;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.device.DeviceService;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.operation.OperationQueryService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/device")
public class DiffController {
    private final AccountRepository accountRepository;
    private final OperationService operationService;
    private final DeviceStatusService deviceStatusService;
    private final DeviceSubTypeService deviceSubTypeService;
    private final DeviceService deviceService;
    private final OperationQueryService operationQueryService;



    public DiffController(AccountRepository accountRepository, OperationService operationService, DeviceStatusService deviceStatusService, DeviceSubTypeService deviceSubTypeService, DeviceService deviceService, OperationQueryService operationQueryService) {
        this.accountRepository = accountRepository;
        this.operationService = operationService;
        this.deviceStatusService = deviceStatusService;
        this.deviceSubTypeService = deviceSubTypeService;
        this.deviceService = deviceService;
        this.operationQueryService = operationQueryService;
    }

    @GetMapping("/mone-board")
    public String showMOneDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = operationQueryService
                .findOperationsByStatusNames(Set.of(CREATE.getCode()));

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);

        return "operation/board/mone-board";
    }

    @PostMapping("/mone-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeMOne(@RequestParam UUID deviceId,
                                          Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        Operation operation = operationService.findById(deviceId).orElseThrow(OperationNotFoundException::new);

        Boolean isInstallation = deviceSubTypeService.findIsSideTwoById(operation);
        UUID operationIdTech = null;

        operationIdTech = operationService.completeOperationWithoutDescription(deviceId, account, SIDE1.getCode());

        if (!isInstallation ) {
            operationIdTech =  operationService.completeOperationWithoutDescription(operationIdTech, account, TECHNICAL3.getCode());
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/mtwo-board")
    public String showMTwoDeviceForm(Model model, Authentication authentication) {

        List<Operation> devices = operationQueryService
                .findOperationsByStatusNames(Set.of(SIDE1.getCode()));

        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        model.addAttribute("currentUser", account);

        return "operation/board/mtwo-board";
    }

    @PostMapping("/mtwo-board/complete")
    @ResponseBody
    public ResponseEntity<?> completeMTwo(@RequestParam UUID deviceId,
                                          Authentication authentication) {

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        operationService.completeOperationWithoutDescription(deviceId, account, SIDE2.getCode());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/ready-board")
    public String showReadyBoard(Model model, Authentication authentication) {

        List<Operation> devices = operationQueryService
                .findOperationsByStatusNames(Set.of(READY.getCode()));


        List<Operation> sortDevices =
                devices.stream().sorted(Comparator.comparing(Operation::getCreatedTime).reversed()).collect(Collectors.toList());

        model.addAttribute("devices", sortDevices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "board/ready-board";
    }

    @GetMapping("/noready-board")
    public String showNoReadyBoard(Model model, Authentication authentication) {

        List<Device> devices = deviceService.findAll().stream()
                .filter(a -> !a.getIsDeleted())
                .filter(a -> a.getOperations().stream()
                        .noneMatch(op -> op.getStatus().getName().equals(READY.getCode())))
                .toList();


        model.addAttribute("devices", devices);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "board/noready-board";
    }

    @GetMapping("/operation-board/{sn}")
    public String showOperationBoard(Model model, Authentication authentication, @PathVariable String sn) {

        List<Operation> operations =
                operationService.findBySerialNumber(sn).stream().filter(a -> !(a.getStatus().getName().equals(TECHNICAL.getCode())
                        || a.getStatus().getName().equals(TECHNICAL2.getCode()) || a.getStatus().getName().equals(TECHNICAL3.getCode())))
                        .sorted(Comparator.comparing(a -> a.getCreatedTime())).collect(Collectors.toList());

        model.addAttribute("operations", operations);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "board/all-operation-board";
    }

    @GetMapping("/ready-board-temp")
    public String showReadyBoardTemp(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestParam(value = "search", required = false) String search,
            Model model,
            Authentication authentication) {

        UUID readyStatusId = deviceStatusService.findByName(READY.getCode()).getId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());

        Page<Operation> devicePage;


        if (search != null && !search.trim().isEmpty()) {
            devicePage = operationService.findByStatusIdAndSerialNumberContainingIgnoreCase(
                    readyStatusId, search.trim(), pageable);
        } else {
            devicePage = operationService.findByStatusId(readyStatusId, pageable);
        }

        List<Operation> sortDevices = devicePage.getContent();

        model.addAttribute("devices", sortDevices);
        model.addAttribute("devicePage", devicePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", devicePage.getTotalPages());
        model.addAttribute("totalElements", devicePage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("searchTerm", search);

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "ready-board-temp";
    }
}
