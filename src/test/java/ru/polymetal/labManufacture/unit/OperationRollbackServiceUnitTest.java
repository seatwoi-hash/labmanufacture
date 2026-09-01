package ru.polymetal.labManufacture.unit;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.models.OperationStatusRoute;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.data.repository.OperationRepository;
import ru.polymetal.labManufacture.data.repository.OperationStatusRouteRepository;
import ru.polymetal.labManufacture.exception.OperationRollbackException;
import ru.polymetal.labManufacture.service.operation.OperationService;
import ru.polymetal.labManufacture.service.operation.SubtypeOperationRoutePolicy;
import ru.polymetal.labManufacture.service.operation.Impl.OperationRollbackServiceImpl;

import java.util.Optional;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Unit-тесты универсального возврата операций.
 *
 * @author Tatarinov Anton
 */
public class OperationRollbackServiceUnitTest {

    @Mock
    private OperationRepository operationRepository;
    @Mock
    private OperationStatusRouteRepository routeRepository;
    @Mock
    private OperationService operationService;
    @Mock
    private SubtypeOperationRoutePolicy subtypeOperationRoutePolicy;
    @Mock
    private OperationStatusRoute operationStatusRoute;

    private AutoCloseable mocks;
    private OperationRollbackServiceImpl rollbackService;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        rollbackService = new OperationRollbackServiceImpl(
                operationRepository, routeRepository, operationService, subtypeOperationRoutePolicy,
                Clock.fixed(Instant.parse("2026-08-31T12:00:00Z"), ZoneOffset.UTC));
        when(routeRepository.findFirstByCurrentStatus_NameOrderById(anyString()))
                .thenReturn(Optional.of(operationStatusRoute));
        when(operationStatusRoute.getNextOperationName()).thenReturn("Разрешённый этап");
        when(subtypeOperationRoutePolicy.isAllowed(nullable(DeviceSubType.class), anyString())).thenReturn(true);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void rollbackReturnsToPreviousHistoricalStatus() {
        UUID operationId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        OperationStatus previousStatus = status("Side2");
        OperationStatus currentStatus = status("Quality_check_№1");
        Operation current = operation(deviceId, currentStatus, false);
        Operation previous = operation(deviceId, previousStatus, true);
        Account account = Account.builder().username("quality").build();

        when(operationService.findById(operationId)).thenReturn(Optional.of(current));
        when(operationRepository.findFirstByDevice_IdAndIsDeletedTrueOrderByDeletedAtDesc(deviceId))
                .thenReturn(Optional.of(previous));
        when(routeRepository.areAdjacent(currentStatus.getId(), previousStatus.getId())).thenReturn(true);
        when(operationService.completeRollbackOperation(
                operationId, account, "Side2", "Возвращён - ошибка монтажа", operationId))
                .thenReturn(resultId);

        UUID actual = rollbackService.rollback(operationId, account, " ошибка монтажа ");

        assertEquals(actual, resultId);
        verify(operationService).completeRollbackOperation(
                operationId, account, "Side2", "Возвращён - ошибка монтажа", operationId);
    }

    @Test
    public void rollbackRejectsStatusesMissingFromRouteTable() {
        UUID operationId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        OperationStatus previousStatus = status("created");
        OperationStatus currentStatus = status("Quality_check_№6");
        Operation current = operation(deviceId, currentStatus, false);
        Operation previous = operation(deviceId, previousStatus, true);
        Account account = Account.builder().username("admin").build();

        when(operationService.findById(operationId)).thenReturn(Optional.of(current));
        when(operationRepository.findFirstByDevice_IdAndIsDeletedTrueOrderByDeletedAtDesc(deviceId))
                .thenReturn(Optional.of(previous));
        when(routeRepository.areAdjacent(currentStatus.getId(), previousStatus.getId())).thenReturn(false);

        expectThrows(OperationRollbackException.class,
                () -> rollbackService.rollback(operationId, account, "invalid"));

        verify(operationService, never()).completeRollbackOperation(
                operationId, account, previousStatus.getName(), "Возвращён - invalid", operationId);
    }

    @Test
    public void rollbackRejectsOperationWithoutHistory() {
        UUID operationId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Operation current = operation(deviceId, status("created"), false);
        Account account = Account.builder().username("operator").build();

        when(operationService.findById(operationId)).thenReturn(Optional.of(current));
        when(operationRepository.findFirstByDevice_IdAndIsDeletedTrueOrderByDeletedAtDesc(deviceId))
                .thenReturn(Optional.empty());

        expectThrows(OperationRollbackException.class,
                () -> rollbackService.rollback(operationId, account, "first operation"));
        verify(routeRepository, never()).areAdjacent(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void rollbackToReturnsToSelectedHistoricalStage() {
        UUID operationId = UUID.randomUUID();
        UUID targetOperationId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        OperationStatus currentStatus = status("Test2");
        OperationStatus targetStatus = status("Side2");
        Operation current = operation(deviceId, currentStatus, false);
        current.setCreatedTime(LocalDateTime.of(2026, 8, 30, 12, 0));
        Operation target = operation(deviceId, targetStatus, true);
        target.setCreatedTime(LocalDateTime.of(2026, 8, 29, 12, 0));
        Account account = Account.builder().username("admin").build();

        when(operationService.findById(operationId)).thenReturn(Optional.of(current));
        when(operationService.findById(targetOperationId)).thenReturn(Optional.of(target));
        when(operationService.completeRollbackOperation(
                operationId, account, targetStatus.getName(), "Возвращён - повторная проверка", operationId))
                .thenReturn(resultId);

        UUID actual = rollbackService.rollbackTo(
                operationId, targetOperationId, account, "повторная проверка");

        assertEquals(actual, resultId);
        verify(operationService).completeRollbackOperation(
                operationId, account, targetStatus.getName(), "Возвращён - повторная проверка", operationId);
        verify(routeRepository, never()).areAdjacent(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @DataProvider
    public Object[][] subtypeRouteFlags() {
        return new Object[][]{
                {false, true, true, "Technical3"},
                {true, false, true, "Technical"},
                {true, true, false, "Technical2"},
                {true, true, true, "Side2"}
        };
    }

    @Test(dataProvider = "subtypeRouteFlags")
    public void rollbackToSupportsSubtypeRouteFlags(boolean isSideTwo,
                                                    boolean isInstallationOne,
                                                    boolean isTestTwo,
                                                    String targetStatusName) {
        UUID operationId = UUID.randomUUID();
        UUID targetOperationId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        DeviceSubType subtype = DeviceSubType.builder()
                .id(UUID.randomUUID())
                .isSideTwo(isSideTwo)
                .isInstallationOne(isInstallationOne)
                .isTestTwo(isTestTwo)
                .build();
        Device device = Device.builder().id(deviceId).subtype(subtype).build();
        OperationStatus currentStatus = status("ready");
        OperationStatus targetStatus = status(targetStatusName);
        Operation current = Operation.builder()
                .device(device)
                .status(currentStatus)
                .isDeleted(false)
                .createdTime(LocalDateTime.of(2026, 8, 30, 12, 0))
                .build();
        Operation target = Operation.builder()
                .device(device)
                .status(targetStatus)
                .isDeleted(true)
                .createdTime(LocalDateTime.of(2026, 8, 29, 12, 0))
                .build();
        Account account = Account.builder().username("admin").build();

        when(operationService.findById(operationId)).thenReturn(Optional.of(current));
        when(operationService.findById(targetOperationId)).thenReturn(Optional.of(target));
        when(operationService.completeRollbackOperation(
                operationId, account, targetStatusName, "Возвращён - проверка маршрута", operationId))
                .thenReturn(resultId);

        UUID actual = rollbackService.rollbackTo(
                operationId, targetOperationId, account, "проверка маршрута");

        assertEquals(actual, resultId);
        assertEquals(device.getSubtype().getIsSideTwo(), isSideTwo);
        assertEquals(device.getSubtype().getIsInstallationOne(), isInstallationOne);
        assertEquals(device.getSubtype().getIsTestTwo(), isTestTwo);
        verify(operationService).completeRollbackOperation(
                operationId, account, targetStatusName, "Возвращён - проверка маршрута", operationId);
    }

    @Test
    public void rollbackToRejectsStageDisabledForSubtype() {
        UUID operationId = UUID.randomUUID();
        UUID targetOperationId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        DeviceSubType subtype = DeviceSubType.builder().isSideTwo(false).build();
        Device device = Device.builder().id(deviceId).subtype(subtype).build();
        Operation current = Operation.builder()
                .device(device)
                .status(status("ready"))
                .isDeleted(false)
                .createdTime(LocalDateTime.of(2026, 8, 30, 12, 0))
                .build();
        Operation target = Operation.builder()
                .device(device)
                .status(status("Side1"))
                .isDeleted(true)
                .createdTime(LocalDateTime.of(2026, 8, 29, 12, 0))
                .build();

        when(operationService.findById(operationId)).thenReturn(Optional.of(current));
        when(operationService.findById(targetOperationId)).thenReturn(Optional.of(target));
        when(operationStatusRoute.getNextOperationName()).thenReturn("Монтаж \"Сторона 2\"");
        when(subtypeOperationRoutePolicy.isAllowed(subtype, "Монтаж \"Сторона 2\""))
                .thenReturn(false);

        expectThrows(OperationRollbackException.class,
                () -> rollbackService.rollbackTo(
                        operationId, targetOperationId, Account.builder().username("admin").build(), "ошибка"));

        verify(operationService, never()).completeRollbackOperation(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void authorCanCancelLastOperationWithinFifteenMinutes() {
        UUID accountId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();
        UUID previousOperationId = UUID.randomUUID();
        UUID rollbackOperationId = UUID.randomUUID();
        Account author = Account.builder().id(accountId).username("operator").roles(Set.of()).build();
        Device device = Device.builder().id(deviceId).build();
        OperationStatus currentStatus = status("Side2");
        OperationStatus previousStatus = status("Side1");
        Operation current = operation(
                operationId, device, currentStatus, author, false,
                LocalDateTime.of(2026, 8, 31, 11, 50));
        Operation previous = operation(
                previousOperationId, device, previousStatus, author, true,
                LocalDateTime.of(2026, 8, 31, 11, 40));
        List<Operation> history = List.of(current, previous);

        when(operationService.findById(operationId)).thenReturn(Optional.of(current));
        when(operationRepository.findHistoryByDeviceId(deviceId)).thenReturn(history);
        when(operationRepository.findActiveByDeviceIdWithLock(deviceId)).thenReturn(Optional.of(current));
        when(operationService.completeRollbackOperation(
                operationId, author, previousStatus.getName(),
                "Возвращён - Ошибка выполнения ОТК", operationId))
                .thenReturn(rollbackOperationId);

        assertTrue(rollbackService.canCancelOwnLastOperation(operationId, author));
        assertEquals(
                rollbackService.cancelOwnLastOperation(
                        operationId, author, "Ошибка выполнения ОТК"), rollbackOperationId);
        verify(operationService).completeRollbackOperation(
                operationId, author, previousStatus.getName(),
                "Возвращён - Ошибка выполнения ОТК", operationId);
    }

    @Test
    public void hiddenTechnicalOperationDoesNotBlockCancellationOfLastVisibleOperation() {
        UUID accountId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        UUID requestedOperationId = UUID.randomUUID();
        UUID activeTechnicalId = UUID.randomUUID();
        Account author = Account.builder().id(accountId).username("quality").roles(Set.of()).build();
        Device device = Device.builder().id(deviceId).build();
        Operation activeTechnical = operation(
                activeTechnicalId, device, status("Technical"), author, false,
                LocalDateTime.of(2026, 8, 31, 11, 51));
        Operation requested = operation(
                requestedOperationId, device, status("Quality_check_№1"), author, true,
                LocalDateTime.of(2026, 8, 31, 11, 50));
        Operation previous = operation(
                UUID.randomUUID(), device, status("Side2"), author, true,
                LocalDateTime.of(2026, 8, 31, 11, 40));
        List<Operation> history = List.of(activeTechnical, requested, previous);

        when(operationService.findById(requestedOperationId)).thenReturn(Optional.of(requested));
        when(operationRepository.findHistoryByDeviceId(deviceId)).thenReturn(history);
        when(operationRepository.findActiveByDeviceIdWithLock(deviceId))
                .thenReturn(Optional.of(activeTechnical));
        when(operationService.completeRollbackOperation(
                activeTechnicalId, author, previous.getStatus().getName(),
                "Возвращён - Ошибка выполнения ОТК", requestedOperationId))
                .thenReturn(UUID.randomUUID());

        assertTrue(rollbackService.canCancelOwnLastOperation(requestedOperationId, author));
        rollbackService.cancelOwnLastOperation(
                requestedOperationId, author, "Ошибка выполнения ОТК");

        verify(operationService).completeRollbackOperation(
                activeTechnicalId, author, previous.getStatus().getName(),
                "Возвращён - Ошибка выполнения ОТК", requestedOperationId);
    }

    @Test
    public void cancellationIsUnavailableAfterFifteenMinutes() {
        Account author = Account.builder().id(UUID.randomUUID()).username("operator").roles(Set.of()).build();
        Device device = Device.builder().id(UUID.randomUUID()).build();
        Operation expired = operation(
                UUID.randomUUID(), device, status("Side2"), author, false,
                LocalDateTime.of(2026, 8, 31, 11, 44, 59));
        Operation previous = operation(
                UUID.randomUUID(), device, status("Side1"), author, true,
                LocalDateTime.of(2026, 8, 31, 11, 30));

        when(operationService.findById(expired.getId())).thenReturn(Optional.of(expired));
        when(operationRepository.findHistoryByDeviceId(device.getId()))
                .thenReturn(List.of(expired, previous));

        assertFalse(rollbackService.canCancelOwnLastOperation(expired.getId(), author));
    }

    @Test
    public void cancellationIsUnavailableToAnotherUserAndAdministrator() {
        Account author = Account.builder().id(UUID.randomUUID()).username("operator").roles(Set.of()).build();
        Account anotherUser = Account.builder().id(UUID.randomUUID()).username("quality").roles(Set.of()).build();
        Role adminRole = new Role("admin", "Administrator");
        Account admin = Account.builder()
                .id(UUID.randomUUID()).username("admin").roles(Set.of(adminRole)).build();
        Device device = Device.builder().id(UUID.randomUUID()).build();
        Operation current = operation(
                UUID.randomUUID(), device, status("Side2"), author, false,
                LocalDateTime.of(2026, 8, 31, 11, 50));
        Operation previous = operation(
                UUID.randomUUID(), device, status("Side1"), author, true,
                LocalDateTime.of(2026, 8, 31, 11, 40));

        when(operationService.findById(current.getId())).thenReturn(Optional.of(current));
        when(operationRepository.findHistoryByDeviceId(device.getId()))
                .thenReturn(List.of(current, previous));

        assertFalse(rollbackService.canCancelOwnLastOperation(current.getId(), anotherUser));
        assertFalse(rollbackService.canCancelOwnLastOperation(current.getId(), admin));
    }

    @Test
    public void cancellationIsUnavailableForNonLastVisibleOperation() {
        Account author = Account.builder().id(UUID.randomUUID()).username("operator").roles(Set.of()).build();
        Device device = Device.builder().id(UUID.randomUUID()).build();
        Operation latest = operation(
                UUID.randomUUID(), device, status("Side2"), author, false,
                LocalDateTime.of(2026, 8, 31, 11, 55));
        Operation requested = operation(
                UUID.randomUUID(), device, status("Side1"), author, true,
                LocalDateTime.of(2026, 8, 31, 11, 50));
        Operation previous = operation(
                UUID.randomUUID(), device, status("created"), author, true,
                LocalDateTime.of(2026, 8, 31, 11, 40));

        when(operationService.findById(requested.getId())).thenReturn(Optional.of(requested));
        when(operationRepository.findHistoryByDeviceId(device.getId()))
                .thenReturn(List.of(latest, requested, previous));

        assertFalse(rollbackService.canCancelOwnLastOperation(requested.getId(), author));
    }

    @Test
    public void cancellationOperationCannotBeCancelledAgain() {
        Account author = Account.builder().id(UUID.randomUUID()).username("operator").roles(Set.of()).build();
        Operation rollback = Operation.builder()
                .id(UUID.randomUUID())
                .account(author)
                .isRollback(true)
                .build();
        when(operationService.findById(rollback.getId())).thenReturn(Optional.of(rollback));

        assertFalse(rollbackService.canCancelOwnLastOperation(rollback.getId(), author));
    }

    private OperationStatus status(String name) {
        return OperationStatus.builder().id(UUID.randomUUID()).name(name).build();
    }

    private Operation operation(UUID deviceId, OperationStatus status, boolean deleted) {
        return Operation.builder()
                .device(Device.builder().id(deviceId).build())
                .status(status)
                .isDeleted(deleted)
                .build();
    }

    private Operation operation(UUID id, Device device, OperationStatus status,
                                Account account, boolean deleted, LocalDateTime createdTime) {
        return Operation.builder()
                .id(id)
                .device(device)
                .status(status)
                .account(account)
                .isDeleted(deleted)
                .createdTime(createdTime)
                .build();
    }
}
