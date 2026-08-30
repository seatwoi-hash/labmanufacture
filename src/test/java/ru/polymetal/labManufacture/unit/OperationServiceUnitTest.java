package ru.polymetal.labManufacture.unit;

import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.repository.OperationRepository;
import ru.polymetal.labManufacture.data.repository.OperationStatusRepository;
import ru.polymetal.labManufacture.data.repository.OperationStatusRouteRepository;
import ru.polymetal.labManufacture.data.repository.RoleOperationStatusAccessRepository;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.operation.Impl.OperationServiceImpl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit-тесты переходов производственных операций.
 *
 * @author Tatarinov Anton
 */
public class OperationServiceUnitTest {

    @Mock
    private OperationRepository operationRepository;
    @Mock
    private DeviceStatusService deviceStatusService;
    @Mock
    private OperationStatusRepository operationStatusRepository;
    @Mock
    private OperationStatusRouteRepository operationStatusRouteRepository;
    @Mock
    private RoleOperationStatusAccessRepository roleOperationStatusAccessRepository;

    private AutoCloseable mocks;
    private OperationServiceImpl service;
    private Clock clock;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        clock = Clock.fixed(Instant.parse("2026-08-30T10:15:30Z"), ZoneOffset.UTC);
        service = new OperationServiceImpl(
                operationRepository,
                deviceStatusService,
                operationStatusRepository,
                roleOperationStatusAccessRepository,
                operationStatusRouteRepository,
                clock
        );
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void completeOperationCreatesOneNewVersionWithCurrentTime() {
        UUID sourceId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        Device device = Device.builder().id(UUID.randomUUID()).serialNumber("SN-1").build();
        Operation source = Operation.builder()
                .id(sourceId)
                .device(device)
                .createdTime(LocalDateTime.of(2026, 8, 1, 8, 0))
                .isDeleted(false)
                .build();
        OperationStatus targetStatus = OperationStatus.builder().name("Test").build();
        Account actor = Account.builder().username("operator").build();

        when(operationRepository.findByIdWithLock(sourceId)).thenReturn(Optional.of(source));
        when(deviceStatusService.findByName("Test")).thenReturn(targetStatus);
        when(operationRepository.save(any(Operation.class))).thenAnswer(invocation -> {
            Operation operation = invocation.getArgument(0);
            operation.setId(resultId);
            return operation;
        });

        UUID actualId = service.completeOperationWithDescription(sourceId, actor, "Test", "done");

        assertEquals(actualId, resultId);
        assertTrue(source.getIsDeleted());
        assertNotNull(source.getDeletedAt());
        ArgumentCaptor<Operation> savedOperation = ArgumentCaptor.forClass(Operation.class);
        verify(operationRepository, times(1)).save(savedOperation.capture());
        assertEquals(savedOperation.getValue().getCreatedTime(), LocalDateTime.now(clock));
        assertFalse(savedOperation.getValue().getIsRollback());
    }

    @Test
    public void completeRollbackOperationMarksNewVersionAsRollback() {
        UUID sourceId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        Operation source = Operation.builder()
                .id(sourceId)
                .device(Device.builder().id(UUID.randomUUID()).serialNumber("SN-2").build())
                .isDeleted(false)
                .build();
        OperationStatus targetStatus = OperationStatus.builder().name("Technical").build();
        Account actor = Account.builder().username("admin").build();

        when(operationRepository.findByIdWithLock(sourceId)).thenReturn(Optional.of(source));
        when(deviceStatusService.findByName("Technical")).thenReturn(targetStatus);
        when(operationRepository.save(any(Operation.class))).thenAnswer(invocation -> {
            Operation operation = invocation.getArgument(0);
            operation.setId(resultId);
            return operation;
        });

        UUID actualId = service.completeRollbackOperation(
                sourceId, actor, "Technical", "Возвращён - повторная проверка");

        assertEquals(actualId, resultId);
        ArgumentCaptor<Operation> savedOperation = ArgumentCaptor.forClass(Operation.class);
        verify(operationRepository).save(savedOperation.capture());
        assertTrue(savedOperation.getValue().getIsRollback());
    }
}
