package ru.polymetal.labManufacture.unit;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.repository.OperationRepository;
import ru.polymetal.labManufacture.data.repository.OperationStatusRouteRepository;
import ru.polymetal.labManufacture.exception.OperationRollbackException;
import ru.polymetal.labManufacture.service.operation.OperationService;
import ru.polymetal.labManufacture.service.operation.Impl.OperationRollbackServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    private AutoCloseable mocks;
    private OperationRollbackServiceImpl rollbackService;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        rollbackService = new OperationRollbackServiceImpl(
                operationRepository, routeRepository, operationService);
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
        when(operationService.completeOperationWithDescription(
                operationId, account, "Side2", "Возвращён - ошибка монтажа"))
                .thenReturn(resultId);

        UUID actual = rollbackService.rollback(operationId, account, " ошибка монтажа ");

        assertEquals(actual, resultId);
        verify(operationService).completeOperationWithDescription(
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

        verify(operationService, never()).completeOperationWithDescription(
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
