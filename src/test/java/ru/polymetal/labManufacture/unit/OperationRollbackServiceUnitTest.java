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
import ru.polymetal.labManufacture.data.repository.OperationRepository;
import ru.polymetal.labManufacture.data.repository.OperationStatusRouteRepository;
import ru.polymetal.labManufacture.exception.OperationRollbackException;
import ru.polymetal.labManufacture.service.operation.OperationService;
import ru.polymetal.labManufacture.service.operation.SubtypeOperationRoutePolicy;
import ru.polymetal.labManufacture.service.operation.Impl.OperationRollbackServiceImpl;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.testng.Assert.assertEquals;
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
                operationRepository, routeRepository, operationService, subtypeOperationRoutePolicy);
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
                operationId, account, "Side2", "Возвращён - ошибка монтажа"))
                .thenReturn(resultId);

        UUID actual = rollbackService.rollback(operationId, account, " ошибка монтажа ");

        assertEquals(actual, resultId);
        verify(operationService).completeRollbackOperation(
                operationId, account, "Side2", "Возвращён - ошибка монтажа");
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
                operationId, account, previousStatus.getName(), "Возвращён - invalid");
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
                operationId, account, targetStatus.getName(), "Возвращён - повторная проверка"))
                .thenReturn(resultId);

        UUID actual = rollbackService.rollbackTo(
                operationId, targetOperationId, account, "повторная проверка");

        assertEquals(actual, resultId);
        verify(operationService).completeRollbackOperation(
                operationId, account, targetStatus.getName(), "Возвращён - повторная проверка");
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
                operationId, account, targetStatusName, "Возвращён - проверка маршрута"))
                .thenReturn(resultId);

        UUID actual = rollbackService.rollbackTo(
                operationId, targetOperationId, account, "проверка маршрута");

        assertEquals(actual, resultId);
        assertEquals(device.getSubtype().getIsSideTwo(), isSideTwo);
        assertEquals(device.getSubtype().getIsInstallationOne(), isInstallationOne);
        assertEquals(device.getSubtype().getIsTestTwo(), isTestTwo);
        verify(operationService).completeRollbackOperation(
                operationId, account, targetStatusName, "Возвращён - проверка маршрута");
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
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
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
}
