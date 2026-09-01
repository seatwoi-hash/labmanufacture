package ru.polymetal.labManufacture.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.exception.OperationRollbackException;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.operation.OperationRollbackService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import ru.polymetal.labManufacture.service.operation.SubtypeOperationRoutePolicy;

import java.util.UUID;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.READY;

/**
 * Административная страница возврата производственных операций.
 *
 * @author Tatarinov Anton
 */
@Controller
@RequestMapping("/admin/operations/rollback")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminOperationRollbackController {

    private final AccountRepository accountRepository;
    private final OperationService operationService;
    private final OperationRollbackService operationRollbackService;
    private final SubtypeOperationRoutePolicy subtypeOperationRoutePolicy;

    @GetMapping
    public String showRollbackPage(@RequestParam(defaultValue = "production") String tab,
                                   @RequestParam(defaultValue = "") String search,
                                   @RequestParam(defaultValue = "date") String sort,
                                   @RequestParam(defaultValue = "desc") String direction,
                                   Model model,
                                   Authentication authentication) {
        Account account = findAccount(authentication);
        List<Operation> operations = new ArrayList<>(
                operationService.findActiveOperationsForRollback());
        boolean readyTab = "ready".equalsIgnoreCase(tab);
        String normalizedSearch = search.trim().toLowerCase(Locale.ROOT);

        operations.removeIf(operation -> {
            boolean ready = READY.matches(operation.getStatus().getName());
            boolean wrongTab = readyTab != ready;
            boolean doesNotMatch = !normalizedSearch.isEmpty()
                    && !operation.getDevice().getSerialNumber().toLowerCase(Locale.ROOT)
                    .contains(normalizedSearch);
            return wrongTab || doesNotMatch;
        });
        operations.sort(operationComparator(sort, direction));

        List<UUID> deviceIds = operations.stream()
                .map(operation -> operation.getDevice().getId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, List<Operation>> rollbackTargets =
                buildRollbackTargets(operationService.findRollbackTargetsByDeviceIds(deviceIds));
        Map<UUID, String> rollbackTargetNames = buildRollbackTargetNames(rollbackTargets);
        Map<UUID, String> operationDisplayNames = operations.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Operation::getId,
                        this::getOperationDisplayName,
                        (first, second) -> first,
                        LinkedHashMap::new));

        model.addAttribute("currentUser", account);
        model.addAttribute("operations", operations);
        model.addAttribute("rollbackTargets", rollbackTargets);
        model.addAttribute("rollbackTargetNames", rollbackTargetNames);
        model.addAttribute("operationDisplayNames", operationDisplayNames);
        model.addAttribute("activeTab", readyTab ? "ready" : "production");
        model.addAttribute("search", search.trim());
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        return "admin/operation-rollback";
    }

    @PostMapping
    public String rollback(@RequestParam UUID operationId,
                           @RequestParam UUID targetOperationId,
                           @RequestParam String comment,
                           @RequestParam(defaultValue = "production") String tab,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        String normalizedComment = comment == null ? "" : comment.trim();
        if (normalizedComment.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Укажите комментарий возврата");
            return "redirect:/admin/operations/rollback";
        }

        try {
            operationRollbackService.rollbackTo(
                    operationId, targetOperationId, findAccount(authentication), normalizedComment);
            redirectAttributes.addFlashAttribute("success", "Операция успешно возвращена назад");
        } catch (OperationRollbackException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        redirectAttributes.addAttribute("tab", tab);
        return "redirect:/admin/operations/rollback";
    }

    private Account findAccount(Authentication authentication) {
        return accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(authentication.getName()));
    }

    private Comparator<Operation> operationComparator(
            String sort, String direction) {
        Comparator<Operation> comparator = switch (sort) {
            case "serial" -> Comparator.comparing(operation -> operation.getDevice().getSerialNumber(),
                    String.CASE_INSENSITIVE_ORDER);
            case "status" -> Comparator.comparing(this::getOperationDisplayName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            default -> Comparator.comparing(
                    Operation::getCreatedTime,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        };
        return "asc".equalsIgnoreCase(direction) ? comparator : comparator.reversed();
    }

    private Map<UUID, List<Operation>> buildRollbackTargets(List<Operation> history) {
        Map<UUID, LinkedHashMap<UUID, Operation>> grouped =
                new LinkedHashMap<>();
        for (Operation operation : history) {
            if (Boolean.TRUE.equals(operation.getIsRollback())
                    || operation.getDevice() == null
                    || operation.getStatus() == null) {
                continue;
            }
            String targetStageName = operationService.getNextStatus(operation.getStatus());
            if (!subtypeOperationRoutePolicy.isAllowed(
                    operation.getDevice().getSubtype(), targetStageName)) {
                continue;
            }
            grouped.computeIfAbsent(operation.getDevice().getId(), ignored -> new LinkedHashMap<>())
                    .putIfAbsent(operation.getStatus().getId(), operation);
        }

        Map<UUID, List<Operation>> result = new LinkedHashMap<>();
        grouped.forEach((deviceId, statuses) -> result.put(deviceId, List.copyOf(statuses.values())));
        return result;
    }

    private Map<UUID, String> buildRollbackTargetNames(Map<UUID, List<Operation>> rollbackTargets) {
        Map<UUID, String> names = new LinkedHashMap<>();
        rollbackTargets.values().stream()
                .flatMap(List::stream)
                .forEach(operation -> names.put(
                        operation.getId(), operationService.getNextStatus(operation.getStatus())));
        return names;
    }

    private String getOperationDisplayName(Operation operation) {
        String statusName = operation.getStatus().getName();
        if (!READY.matches(statusName)) {
            return operationService.getNextStatus(operation.getStatus());
        }
        String description = operation.getStatus().getDescription();
        return description == null || description.isBlank() ? statusName : description;
    }
}
