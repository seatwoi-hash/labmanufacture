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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
import ru.polymetal.labManufacture.exception.OperationRollbackException;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.device.DeviceService;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.operation.OperationQueryService;
import ru.polymetal.labManufacture.service.operation.OperationRollbackService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MVC-контроллер DiffController.
 *
 * @author Tatarinov Anton
 */
@Controller
@RequestMapping("/device")
public class DiffController {
    private final AccountRepository accountRepository;
    private final OperationService operationService;
    private final DeviceStatusService deviceStatusService;
    private final DeviceSubTypeService deviceSubTypeService;
    private final DeviceService deviceService;
    private final OperationQueryService operationQueryService;
    private final OperationRollbackService operationRollbackService;



    public DiffController(AccountRepository accountRepository, OperationService operationService, DeviceStatusService deviceStatusService, DeviceSubTypeService deviceSubTypeService, DeviceService deviceService, OperationQueryService operationQueryService, OperationRollbackService operationRollbackService) {
        this.accountRepository = accountRepository;
        this.operationService = operationService;
        this.deviceStatusService = deviceStatusService;
        this.deviceSubTypeService = deviceSubTypeService;
        this.deviceService = deviceService;
        this.operationQueryService = operationQueryService;
        this.operationRollbackService = operationRollbackService;
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
                .filter(device -> !Boolean.TRUE.equals(device.getIsDeleted()))
                .filter(device -> device.getOperations().stream()
                        .filter(operation -> !Boolean.TRUE.equals(operation.getIsDeleted()))
                        .noneMatch(operation -> READY.matches(operation.getStatus().getName())))
                .toList();


        model.addAttribute("devices", devices);
        model.addAttribute("deviceDisplayNames", buildDeviceDisplayNames(devices));

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));


        model.addAttribute("currentUser", account);

        return "board/noready-board";
    }

    private Map<UUID, String> buildDeviceDisplayNames(List<Device> devices) {
        Map<UUID, String> displayNames = new LinkedHashMap<>();
        for (Device device : devices) {
            Operation activeOperation = device.getOperations().stream()
                    .filter(operation -> !Boolean.TRUE.equals(operation.getIsDeleted()))
                    .findFirst()
                    .orElse(null);

            if (activeOperation != null && isTechnicalStatus(activeOperation)) {
                displayNames.put(
                        device.getId(), operationService.getPreviousStatus(activeOperation.getStatus()));
                continue;
            }

            Operation displayStatus = device.getDisplayStatus();
            displayNames.put(
                    device.getId(),
                    displayStatus == null || displayStatus.getStatus() == null
                            ? null
                            : displayStatus.getStatus().getDescription());
        }
        return displayNames;
    }

    @GetMapping("/operation-board/{sn}")
    public String showOperationBoard(Model model, Authentication authentication, @PathVariable String sn) {

        List<Operation> operations = operationService.findBySerialNumber(sn).stream()
                .filter(operation -> Boolean.TRUE.equals(operation.getIsRollback())
                        || !isTechnicalStatus(operation))
                .sorted(Comparator.comparing(Operation::getCreatedTime))
                .collect(Collectors.toList());

        Account account =
                accountRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        UUID cancelableOperationId = null;
        if (!operations.isEmpty()) {
            Operation lastVisibleOperation = operations.get(operations.size() - 1);
            if (operationRollbackService.canCancelOwnLastOperation(
                    lastVisibleOperation.getId(), account)) {
                cancelableOperationId = lastVisibleOperation.getId();
            }
        }

        model.addAttribute("operations", operations);
        model.addAttribute("operationDisplayNames", buildOperationDisplayNames(operations));
        model.addAttribute("cancelableOperationId", cancelableOperationId);
        model.addAttribute("currentUser", account);

        return "board/all-operation-board";
    }

    @PostMapping("/operation-board/{sn}/cancel")
    public String cancelOwnLastOperation(@PathVariable String sn,
                                         @RequestParam UUID operationId,
                                         @RequestParam String comment,
                                         Authentication authentication,
                                         RedirectAttributes redirectAttributes) {
        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(authentication.getName()));
        try {
            operationRollbackService.cancelOwnLastOperation(operationId, account, comment);
            redirectAttributes.addFlashAttribute("success", "Операция успешно отменена");
        } catch (OperationRollbackException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/device/operation-board/" + sn;
    }

    private boolean isTechnicalStatus(Operation operation) {
        if (operation.getStatus() == null || operation.getStatus().getName() == null) {
            return false;
        }
        String statusName = operation.getStatus().getName();
        return TECHNICAL.matches(statusName)
                || TECHNICAL2.matches(statusName)
                || TECHNICAL3.matches(statusName);
    }

    private Map<UUID, String> buildOperationDisplayNames(List<Operation> operations) {
        Map<UUID, String> displayNames = new LinkedHashMap<>();
        for (Operation operation : operations) {
            String displayName = operation.getStatus().getDescription();
            if (Boolean.TRUE.equals(operation.getIsRollback()) && isTechnicalStatus(operation)) {
                displayName = operationService.getPreviousStatus(operation.getStatus());
            }
            displayNames.put(operation.getId(), displayName);
        }
        return displayNames;
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
