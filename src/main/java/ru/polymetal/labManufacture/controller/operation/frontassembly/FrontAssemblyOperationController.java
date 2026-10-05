package ru.polymetal.labManufacture.controller.operation.frontassembly;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.polymetal.labManufacture.constant.FrontAssemblyStatusCode;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.FrontAssemblyFlowService;

import java.util.List;
import java.util.UUID;

import static ru.polymetal.labManufacture.constant.FrontAssemblyStatusCode.*;

@Controller
@RequestMapping("/device/front-assembly")
@PreAuthorize("hasRole('ADMIN')")
public class FrontAssemblyOperationController {
    private final AccountRepository accountRepository;
    private final FrontAssemblyFlowService flowService;

    public FrontAssemblyOperationController(AccountRepository accountRepository, FrontAssemblyFlowService flowService) {
        this.accountRepository = accountRepository;
        this.flowService = flowService;
    }

    @GetMapping("/otk")
    public String showOtk(Model model, Authentication authentication) {
        populate(model, authentication, List.of(QUALITY_CHECK_1, QUALITY_CHECK_2, QUALITY_CHECK_3));
        return "device/front-assembly-otk";
    }

    @GetMapping("/repair")
    public String showRepair(Model model, Authentication authentication) {
        return stage(model, authentication, List.of(REPAIR_1, REPAIR_2), "repair",
                "Ремонт передней части моноблока", "Сборки для ремонта", "Нет сборок для ремонта");
    }

    @GetMapping("/testing")
    public String showTesting(Model model, Authentication authentication) {
        return stage(model, authentication, List.of(TEST_1, TEST_2), "testing",
                "Тестирование передней части моноблока", "Сборки для тестирования", "Нет сборок для тестирования");
    }

    @GetMapping("/diagnostics")
    public String showDiagnostics(Model model, Authentication authentication) {
        return stage(model, authentication, List.of(DIAGNOSTICS), "diagnostics",
                "Диагностика передней части моноблока", "Сборки для диагностики", "Нет сборок для диагностики");
    }

    @GetMapping("/ready")
    public String showReady(Model model, Authentication authentication) {
        return stage(model, authentication, List.of(READY), "ready",
                "Готовые сборки передней части моноблока", "Готовые сборки", "Готовых сборок пока нет");
    }

    @PostMapping("/{assemblyId}/transition")
    public String transition(@PathVariable UUID assemblyId, @RequestParam String action,
                             @RequestParam(required = false) String description,
                             @RequestParam String returnTo, Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            flowService.transition(assemblyId, action, description, currentAccount(authentication));
            redirectAttributes.addFlashAttribute("success", "Сборка передана на следующий этап");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/device/front-assembly/" + safeReturnTo(returnTo);
    }

    private String stage(Model model, Authentication authentication, List<FrontAssemblyStatusCode> statuses,
                         String stage, String pageTitle, String tableTitle, String emptyMessage) {
        populate(model, authentication, statuses);
        model.addAttribute("stage", stage);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("tableTitle", tableTitle);
        model.addAttribute("emptyMessage", emptyMessage);
        return "device/front-assembly-stage";
    }

    private void populate(Model model, Authentication authentication, List<FrontAssemblyStatusCode> statuses) {
        model.addAttribute("currentUser", currentAccount(authentication));
        model.addAttribute("operations", flowService.findByStatuses(statuses));
    }

    private Account currentAccount(Authentication authentication) {
        return accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(authentication.getName()));
    }

    private String safeReturnTo(String returnTo) {
        return switch (returnTo) {
            case "otk", "repair", "testing", "diagnostics", "ready" -> returnTo;
            default -> "otk";
        };
    }
}
