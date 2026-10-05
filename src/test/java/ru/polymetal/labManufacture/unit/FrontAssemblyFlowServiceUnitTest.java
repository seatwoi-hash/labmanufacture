package ru.polymetal.labManufacture.unit;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.*;
import ru.polymetal.labManufacture.constant.FrontAssemblyStatusCode;
import ru.polymetal.labManufacture.data.models.*;
import ru.polymetal.labManufacture.data.repository.*;
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

        service.transition(assemblyId, action, "Комментарий", new Account());

        assertTrue(current.getIsDeleted());
        assertNotNull(current.getDeletedAt());
        ArgumentCaptor<FrontAssemblyOperation> captor = ArgumentCaptor.forClass(FrontAssemblyOperation.class);
        verify(operationRepository, times(2)).save(captor.capture());
        FrontAssemblyOperation next = captor.getAllValues().get(1);
        assertSame(next.getFrontAssembly(), assembly);
        assertSame(next.getStatus(), expectedStatus);
        assertEquals(next.getDescription(), "Комментарий");
    }

    private FrontAssemblyStatus status(FrontAssemblyStatusCode code) {
        FrontAssemblyStatus status = new FrontAssemblyStatus();
        status.setName(code.getCode());
        status.setDescription(code.getCode());
        return status;
    }
}
