package ru.polymetal.labManufacture.unit;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.*;
import ru.polymetal.labManufacture.constant.FrontAssemblyStatusCode;
import ru.polymetal.labManufacture.data.models.*;
import ru.polymetal.labManufacture.data.repository.*;
import ru.polymetal.labManufacture.dto.FrontAssemblyCreateDto;
import ru.polymetal.labManufacture.service.FrontAssemblyFlowService;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class FrontAssemblyFlowServiceUnitTest {
    @Mock private FrontAssemblyRepository assemblyRepository;
    @Mock private FrontAssemblyTypeRepository typeRepository;
    @Mock private FrontAssemblyStatusRepository statusRepository;
    @Mock private FrontAssemblyOperationRepository operationRepository;
    @Mock private DeviceRepository deviceRepository;
    @Mock private OperationRepository boardOperationRepository;
    private AutoCloseable mocks;
    private FrontAssemblyFlowService service;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new FrontAssemblyFlowService(assemblyRepository, typeRepository, statusRepository,
                operationRepository, deviceRepository, boardOperationRepository);
        when(operationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterMethod
    public void tearDown() throws Exception { mocks.close(); }

    @DataProvider
    public Object[][] transitions() {
        return new Object[][]{
                {FrontAssemblyStatusCode.QUALITY_CHECK_1, "passed", FrontAssemblyStatusCode.TEST_1},
                {FrontAssemblyStatusCode.QUALITY_CHECK_1, "failed", FrontAssemblyStatusCode.REPAIR_1},
                {FrontAssemblyStatusCode.REPAIR_1, "complete", FrontAssemblyStatusCode.QUALITY_CHECK_2},
                {FrontAssemblyStatusCode.QUALITY_CHECK_2, "passed", FrontAssemblyStatusCode.TEST_1},
                {FrontAssemblyStatusCode.TEST_1, "failed", FrontAssemblyStatusCode.DIAGNOSTICS},
                {FrontAssemblyStatusCode.DIAGNOSTICS, "test", FrontAssemblyStatusCode.TEST_2},
                {FrontAssemblyStatusCode.DIAGNOSTICS, "repair", FrontAssemblyStatusCode.REPAIR_2},
                {FrontAssemblyStatusCode.REPAIR_2, "complete", FrontAssemblyStatusCode.QUALITY_CHECK_3},
                {FrontAssemblyStatusCode.QUALITY_CHECK_3, "passed", FrontAssemblyStatusCode.TEST_2},
                {FrontAssemblyStatusCode.TEST_2, "passed", FrontAssemblyStatusCode.READY}
        };
    }

    @Test(dataProvider = "transitions")
    public void transitionCreatesExpectedNextOperation(FrontAssemblyStatusCode currentCode,
                                                       String action,
                                                       FrontAssemblyStatusCode expectedCode) {
        UUID assemblyId = UUID.randomUUID();
        FrontAssembly assembly = new FrontAssembly();
        assembly.setId(assemblyId);
        FrontAssemblyStatus currentStatus = status(currentCode);
        FrontAssemblyOperation current = new FrontAssemblyOperation();
        current.setFrontAssembly(assembly);
        current.setStatus(currentStatus);
        current.setIsDeleted(false);
        FrontAssemblyStatus expectedStatus = status(expectedCode);
        when(operationRepository.findActiveForUpdate(assemblyId)).thenReturn(Optional.of(current));
        when(statusRepository.findByName(expectedCode.getCode())).thenReturn(Optional.of(expectedStatus));
        FrontAssemblyStatusCode failureCode = failureStatus(currentCode, action);
        if (failureCode != null) {
            when(statusRepository.findByName(failureCode.getCode()))
                    .thenReturn(Optional.of(status(failureCode)));
        }

        service.transition(assemblyId, action, "Комментарий", new Account());

        assertTrue(current.getIsDeleted());
        assertNotNull(current.getDeletedAt());
        ArgumentCaptor<FrontAssemblyOperation> captor = ArgumentCaptor.forClass(FrontAssemblyOperation.class);
        verify(operationRepository, times(failureCode == null ? 2 : 4)).save(captor.capture());
        FrontAssemblyOperation next = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertSame(next.getFrontAssembly(), assembly);
        assertSame(next.getStatus(), expectedStatus);
        if (failureCode == null) {
            assertEquals(next.getDescription(), "Комментарий");
            assertEquals(next.getTransitionAction(), action);
        } else {
            FrontAssemblyOperation failed = captor.getAllValues().get(1);
            assertEquals(failed.getStatus().getName(), failureCode.getCode());
            assertEquals(failed.getDescription(), "Комментарий");
            assertEquals(failed.getTransitionAction(), "failed");
            assertTrue(failed.getIsDeleted());
            assertNull(next.getDescription());
            assertNull(next.getTransitionAction());
        }
    }

    @Test
    public void transitionRequiresCommentForActionOtherThanPassed() {
        UUID assemblyId = UUID.randomUUID();
        FrontAssemblyOperation current = new FrontAssemblyOperation();
        current.setStatus(status(FrontAssemblyStatusCode.QUALITY_CHECK_1));
        when(operationRepository.findActiveForUpdate(assemblyId)).thenReturn(Optional.of(current));

        IllegalArgumentException error = expectThrows(
                IllegalArgumentException.class,
                () -> service.transition(assemblyId, "failed", "  ", new Account()));

        assertEquals(error.getMessage(), "Комментарий обязателен для выбранного действия");
        verify(operationRepository, never()).save(any());
    }

    @Test
    public void transitionRejectsUnknownActionBeforeCommentValidation() {
        UUID assemblyId = UUID.randomUUID();
        FrontAssemblyOperation current = new FrontAssemblyOperation();
        current.setStatus(status(FrontAssemblyStatusCode.QUALITY_CHECK_1));
        when(operationRepository.findActiveForUpdate(assemblyId)).thenReturn(Optional.of(current));

        IllegalArgumentException error = expectThrows(
                IllegalArgumentException.class,
                () -> service.transition(assemblyId, "unexpected", "", new Account()));

        assertEquals(error.getMessage(), "Недопустимое действие: unexpected");
        verify(operationRepository, never()).save(any());
    }

    @Test
    public void createRejectsBoardAlreadyUsedByActiveAssembly() {
        UUID typeId = UUID.randomUUID();
        UUID motherboardId = UUID.randomUUID();
        UUID motherboardSubtypeId = UUID.randomUUID();
        UUID keyboardSubtypeId = UUID.randomUUID();
        DeviceSubType motherboardSubtype = DeviceSubType.builder().id(motherboardSubtypeId).build();
        DeviceSubType keyboardSubtype = DeviceSubType.builder().id(keyboardSubtypeId).build();
        FrontAssemblyType type = new FrontAssemblyType();
        type.setMotherboardSubtype(motherboardSubtype);
        type.setKeyboardBoardSubtype(keyboardSubtype);
        Device motherboard = Device.builder()
                .id(motherboardId).serialNumber("MB-001").subtype(motherboardSubtype).build();
        Operation readyOperation = new Operation();
        readyOperation.setStatus(new OperationStatus());
        readyOperation.getStatus().setName("ready");
        FrontAssemblyCreateDto form = new FrontAssemblyCreateDto(
                "CASE-001", typeId, "MB-001", "KB-001");

        when(typeRepository.findByIdAndIsDeletedFalse(typeId)).thenReturn(Optional.of(type));
        when(deviceRepository.findOneBySerialNumberAndIsDeletedFalse("MB-001"))
                .thenReturn(Optional.of(motherboard));
        when(boardOperationRepository.findActiveByDeviceIdWithLock(motherboardId))
                .thenReturn(Optional.of(readyOperation));
        when(assemblyRepository.existsActiveByBoardId(motherboardId)).thenReturn(true);

        IllegalArgumentException error = expectThrows(
                IllegalArgumentException.class, () -> service.create(form, new Account()));

        assertEquals(error.getMessage(), "Плата уже используется в другой сборке");
        verify(assemblyRepository, never()).save(any());
    }

    @Test
    public void deleteInProgressSoftDeletesAssemblyAndActiveOperation() {
        UUID assemblyId = UUID.randomUUID();
        FrontAssembly assembly = new FrontAssembly();
        assembly.setId(assemblyId);
        FrontAssemblyOperation operation = new FrontAssemblyOperation();
        operation.setFrontAssembly(assembly);
        operation.setStatus(status(FrontAssemblyStatusCode.TEST_1));
        operation.setIsDeleted(false);
        when(operationRepository.findActiveForUpdate(assemblyId)).thenReturn(Optional.of(operation));

        service.deleteInProgress(assemblyId);

        assertTrue(operation.getIsDeleted());
        assertNotNull(operation.getDeletedAt());
        assertTrue(assembly.getIsDeleted());
        assertNotNull(assembly.getDeletedAt());
        verify(operationRepository).save(operation);
        verify(assemblyRepository).save(assembly);
    }

    @Test
    public void deleteInProgressRejectsReadyAssembly() {
        UUID assemblyId = UUID.randomUUID();
        FrontAssemblyOperation operation = new FrontAssemblyOperation();
        operation.setFrontAssembly(new FrontAssembly());
        operation.setStatus(status(FrontAssemblyStatusCode.READY));
        when(operationRepository.findActiveForUpdate(assemblyId)).thenReturn(Optional.of(operation));

        IllegalStateException error = expectThrows(
                IllegalStateException.class, () -> service.deleteInProgress(assemblyId));

        assertEquals(error.getMessage(), "Готовую сборку нельзя удалить со страницы сборок в работе");
        verify(operationRepository, never()).save(any());
        verify(assemblyRepository, never()).save(any());
    }

    private FrontAssemblyStatus status(FrontAssemblyStatusCode code) {
        FrontAssemblyStatus status = new FrontAssemblyStatus();
        status.setName(code.getCode());
        status.setDescription(code.getCode());
        return status;
    }

    private FrontAssemblyStatusCode failureStatus(FrontAssemblyStatusCode source, String action) {
        if (!"failed".equals(action)) return null;
        return switch (source) {
            case QUALITY_CHECK_1 -> FrontAssemblyStatusCode.FAIL_QUALITY_CHECK_1;
            case QUALITY_CHECK_2 -> FrontAssemblyStatusCode.FAIL_QUALITY_CHECK_2;
            case QUALITY_CHECK_3 -> FrontAssemblyStatusCode.FAIL_QUALITY_CHECK_3;
            case TEST_1 -> FrontAssemblyStatusCode.FAIL_TEST_1;
            case TEST_2 -> FrontAssemblyStatusCode.FAIL_TEST_2;
            default -> null;
        };
    }
}
