package ru.polymetal.labManufacture.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.exception.UserNotFoundException;
import ru.polymetal.labManufacture.service.operation.OperationRollbackService;

import java.util.UUID;

/**
 * Универсальный MVC-контроллер возврата производственных операций.
 *
 * @author Tatarinov Anton
 */
@Controller
@RequestMapping("/device")
@RequiredArgsConstructor
public class RollbackOtkController {

    private final AccountRepository accountRepository;
    private final OperationRollbackService operationRollbackService;

    @PostMapping({
            "/operations/rollback",
            "/otk1-board/rollback",
            "/otk2-board/rollback",
            "/otk3-board/rollback",
            "/otk4-board/rollback",
            "/otk5-board/rollback",
            "/otk6-board/rollback"
    })
    @ResponseBody
    public ResponseEntity<Void> rollback(@RequestParam UUID deviceId,
                                         @ModelAttribute("device") Operation operation,
                                         Authentication authentication) {
        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        operationRollbackService.rollback(deviceId, account, operation.getDescription());
        return ResponseEntity.ok().build();
    }
}
