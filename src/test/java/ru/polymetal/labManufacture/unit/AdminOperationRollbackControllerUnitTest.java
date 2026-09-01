package ru.polymetal.labManufacture.unit;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.controller.AdminOperationRollbackController;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.service.operation.OperationRollbackService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import ru.polymetal.labManufacture.service.operation.SubtypeOperationRoutePolicy;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Unit-тесты административной страницы возврата операций.
 *
 * @author Tatarinov Anton
 */
public class AdminOperationRollbackControllerUnitTest {

    @Mock private AccountRepository accountRepository;
    @Mock private OperationService operationService;
    @Mock private OperationRollbackService rollbackService;
    @Mock private SubtypeOperationRoutePolicy subtypeOperationRoutePolicy;

    private AutoCloseable mocks;
    private MockMvc mockMvc;
    private Account admin;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        admin = Account.builder().username("admin").build();
        when(accountRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(subtypeOperationRoutePolicy.isAllowed(nullable(DeviceSubType.class), anyString())).thenReturn(true);
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminOperationRollbackController(
                accountRepository, operationService, rollbackService, subtypeOperationRoutePolicy)).build();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void pageContainsActiveOperations() throws Exception {
        when(operationService.findActiveOperationsForRollback()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/operations/rollback").principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/operation-rollback"))
                .andExpect(model().attribute("currentUser", admin))
                .andExpect(model().attribute("operations", Collections.emptyList()));
    }

    @Test
    public void rollbackTargetUsesNextRouteName() throws Exception {
        UUID deviceId = UUID.randomUUID();
        OperationStatus ready = OperationStatus.builder().id(UUID.randomUUID()).name("ready").build();
        OperationStatus sideTwo = OperationStatus.builder().id(UUID.randomUUID()).name("Side2").build();
        Device device = Device.builder().id(deviceId).serialNumber("SN-OTK-1").isDeleted(false).build();
        Operation active = Operation.builder()
                .id(UUID.randomUUID())
                .device(device)
                .status(ready)
                .isDeleted(false)
                .createdTime(LocalDateTime.of(2026, 8, 30, 12, 0))
                .build();
        Operation target = Operation.builder()
                .id(UUID.randomUUID())
                .device(device)
                .status(sideTwo)
                .isDeleted(true)
                .createdTime(LocalDateTime.of(2026, 8, 29, 12, 0))
                .build();

        when(operationService.findActiveOperationsForRollback()).thenReturn(List.of(active));
        when(operationService.findRollbackTargetsByDeviceIds(List.of(deviceId))).thenReturn(List.of(target));
        when(operationService.getNextStatus(sideTwo)).thenReturn("ОТК №1");

        mockMvc.perform(get("/admin/operations/rollback")
                        .param("tab", "ready")
                        .principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("rollbackTargetNames", Map.of(target.getId(), "ОТК №1")));
    }

    @Test
    public void rollbackOperationIsNotAvailableAsRollbackTarget() throws Exception {
        UUID deviceId = UUID.randomUUID();
        OperationStatus ready = OperationStatus.builder().id(UUID.randomUUID()).name("ready").build();
        OperationStatus sideTwo = OperationStatus.builder().id(UUID.randomUUID()).name("Side2").build();
        Device device = Device.builder().id(deviceId).serialNumber("SN-ROLLBACK").isDeleted(false).build();
        Operation active = Operation.builder()
                .id(UUID.randomUUID())
                .device(device)
                .status(ready)
                .isDeleted(false)
                .createdTime(LocalDateTime.of(2026, 8, 30, 12, 0))
                .build();
        Operation rollbackTarget = Operation.builder()
                .id(UUID.randomUUID())
                .device(device)
                .status(sideTwo)
                .isDeleted(true)
                .isRollback(true)
                .createdTime(LocalDateTime.of(2026, 8, 29, 12, 0))
                .build();

        when(operationService.findActiveOperationsForRollback()).thenReturn(List.of(active));
        when(operationService.findRollbackTargetsByDeviceIds(List.of(deviceId)))
                .thenReturn(List.of(rollbackTarget));

        mockMvc.perform(get("/admin/operations/rollback")
                        .param("tab", "ready")
                        .principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("rollbackTargets", Map.of()))
                .andExpect(model().attribute("rollbackTargetNames", Map.of()));

        verify(operationService, never()).getNextStatus(sideTwo);
    }

    @Test
    public void technicalCurrentStatusIsDisplayedAsNextBusinessStage() throws Exception {
        UUID operationId = UUID.randomUUID();
        OperationStatus technical = OperationStatus.builder()
                .id(UUID.randomUUID())
                .name("Technical3")
                .description("Технический 3")
                .build();
        Device device = Device.builder()
                .id(UUID.randomUUID())
                .serialNumber("SN-TECH-3")
                .isDeleted(false)
                .build();
        Operation active = Operation.builder()
                .id(operationId)
                .device(device)
                .status(technical)
                .isDeleted(false)
                .createdTime(LocalDateTime.of(2026, 8, 30, 12, 0))
                .build();

        when(operationService.findActiveOperationsForRollback()).thenReturn(List.of(active));
        when(operationService.findRollbackTargetsByDeviceIds(List.of(device.getId())))
                .thenReturn(Collections.emptyList());
        when(operationService.getNextStatus(technical)).thenReturn("ОТК №1");

        mockMvc.perform(get("/admin/operations/rollback").principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(model().attribute(
                        "operationDisplayNames", Map.of(operationId, "ОТК №1")));
    }

    @Test
    public void regularCurrentStatusIsDisplayedAsNextBusinessStageLikeMainPage() throws Exception {
        UUID operationId = UUID.randomUUID();
        OperationStatus sideTwo = OperationStatus.builder()
                .id(UUID.randomUUID())
                .name("Side2")
                .description("Сторона 2")
                .build();
        Device device = Device.builder()
                .id(UUID.randomUUID())
                .serialNumber("SN-SIDE-2")
                .isDeleted(false)
                .build();
        Operation active = Operation.builder()
                .id(operationId)
                .device(device)
                .status(sideTwo)
                .isDeleted(false)
                .createdTime(LocalDateTime.of(2026, 8, 30, 12, 0))
                .build();

        when(operationService.findActiveOperationsForRollback()).thenReturn(List.of(active));
        when(operationService.findRollbackTargetsByDeviceIds(List.of(device.getId())))
                .thenReturn(Collections.emptyList());
        when(operationService.getNextStatus(sideTwo)).thenReturn("ОТК №1");

        mockMvc.perform(get("/admin/operations/rollback").principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(model().attribute(
                        "operationDisplayNames", Map.of(operationId, "ОТК №1")));
    }

    @Test
    public void rollbackTrimsAndSavesRequiredComment() throws Exception {
        UUID operationId = UUID.randomUUID();
        UUID targetOperationId = UUID.randomUUID();

        mockMvc.perform(post("/admin/operations/rollback")
                        .param("operationId", operationId.toString())
                        .param("targetOperationId", targetOperationId.toString())
                        .param("comment", "  ошибка монтажа  ")
                        .principal(authentication()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/operations/rollback?tab=production"))
                .andExpect(flash().attribute("success", "Операция успешно возвращена назад"));

        verify(rollbackService).rollbackTo(operationId, targetOperationId, admin, "ошибка монтажа");
    }

    @Test
    public void rollbackRejectsBlankComment() throws Exception {
        UUID operationId = UUID.randomUUID();
        UUID targetOperationId = UUID.randomUUID();

        mockMvc.perform(post("/admin/operations/rollback")
                        .param("operationId", operationId.toString())
                        .param("targetOperationId", targetOperationId.toString())
                        .param("comment", "   ")
                        .principal(authentication()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/operations/rollback"))
                .andExpect(flash().attribute("error", "Укажите комментарий возврата"));

        verify(rollbackService, never()).rollbackTo(operationId, targetOperationId, admin, "");
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken("admin", null);
    }
}
