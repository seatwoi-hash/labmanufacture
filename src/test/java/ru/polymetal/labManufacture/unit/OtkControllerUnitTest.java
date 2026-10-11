package ru.polymetal.labManufacture.unit;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.controller.operation.board.OtkController;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.exception.OperationNotFoundException;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.file.FileService;
import ru.polymetal.labManufacture.service.operation.OperationQueryService;
import ru.polymetal.labManufacture.service.operation.OperationService;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_1_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_5_1_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_6;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.READY;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.SIDE2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TECHNICAL;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.WASHING1;

public class OtkControllerUnitTest {

    @Mock private AccountRepository accountRepository;
    @Mock private OperationService operationService;
    @Mock private DeviceSubTypeService deviceSubTypeService;
    @Mock private FileService fileService;
    @Mock private OperationQueryService operationQueryService;

    private AutoCloseable mocks;
    private OtkController controller;
    private Account account;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        account = Account.builder().username("quality").build();
        when(accountRepository.findByUsername("quality")).thenReturn(Optional.of(account));
        controller = new OtkController(
                accountRepository,
                operationService,
                deviceSubTypeService,
                fileService,
                operationQueryService);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void otkOnePassedMovesNonInstallationDeviceThroughTechnicalStatus() throws Exception {
        UUID operationId = UUID.randomUUID();
        UUID qualityOperationId = UUID.randomUUID();
        Operation source = operationWithStatus(SIDE2.getCode());
        Operation form = Operation.builder().description("проверено").build();
        when(operationService.findById(operationId)).thenReturn(Optional.of(source));
        when(deviceSubTypeService.findIsInstallationOneById(source)).thenReturn(false);
        when(operationService.completeOperationWithDescription(
                operationId, account, QUALITY_CHECK_1.getCode(), "проверено"))
                .thenReturn(qualityOperationId);

        controller.completeOtkOne(operationId, "passed", form, authentication());

        verify(operationService).completeOperationWithoutDescription(
                qualityOperationId, account, TECHNICAL.getCode());
    }

    @Test
    public void otkOnePassedDoesNotAddTechnicalStatusForInstallationDevice() throws Exception {
        UUID operationId = UUID.randomUUID();
        UUID qualityOperationId = UUID.randomUUID();
        Operation source = operationWithStatus(SIDE2.getCode());
        Operation form = Operation.builder().description("проверено").build();
        when(operationService.findById(operationId)).thenReturn(Optional.of(source));
        when(deviceSubTypeService.findIsInstallationOneById(source)).thenReturn(true);
        when(operationService.completeOperationWithDescription(
                operationId, account, QUALITY_CHECK_1.getCode(), "проверено"))
                .thenReturn(qualityOperationId);

        controller.completeOtkOne(operationId, "passed", form, authentication());

        verify(operationService, never()).completeOperationWithoutDescription(
                qualityOperationId, account, TECHNICAL.getCode());
    }

    @Test
    public void otkOneFailedRepairUsesRepairFailureStatus() throws Exception {
        UUID operationId = UUID.randomUUID();
        Operation source = operationWithStatus(REPAIR1.getCode());
        Operation form = Operation.builder().description("дефект").build();
        when(operationService.findById(operationId)).thenReturn(Optional.of(source));
        when(deviceSubTypeService.findIsInstallationOneById(source)).thenReturn(false);

        controller.completeOtkOne(operationId, "failed", form, authentication());

        verify(operationService).completeOperationWithDescription(
                operationId, account, FAIL_QUALITY_CHECK_1_1.getCode(), "дефект");
        verify(operationService, never()).completeOperationWithoutDescription(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(account),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    public void otkFiveFailedWashUsesWashFailureStatus() throws Exception {
        UUID operationId = UUID.randomUUID();
        UUID completedOperationId = UUID.randomUUID();
        Operation source = operationWithStatus(WASHING1.getCode());
        Operation form = Operation.builder().description("не отмыто").build();
        when(operationService.findById(operationId)).thenReturn(Optional.of(source));
        when(operationService.completeOperationWithDescription(
                operationId, account, FAIL_QUALITY_CHECK_5_1_1.getCode(), "не отмыто"))
                .thenReturn(completedOperationId);
        when(operationService.findById(completedOperationId)).thenReturn(Optional.of(new Operation()));

        controller.completeOtkFive(operationId, "failed_wash", form, authentication());

        verify(operationService).completeOperationWithDescription(
                operationId, account, FAIL_QUALITY_CHECK_5_1_1.getCode(), "не отмыто");
    }

    @Test
    public void otkFivePassedWashingUsesQualityStatus() throws Exception {
        UUID operationId = UUID.randomUUID();
        UUID completedOperationId = UUID.randomUUID();
        Operation source = operationWithStatus(WASHING1.getCode());
        Operation form = Operation.builder().description("готово").build();
        when(operationService.findById(operationId)).thenReturn(Optional.of(source));
        when(operationService.completeOperationWithDescription(
                operationId, account, QUALITY_CHECK_5.getCode(), "готово"))
                .thenReturn(completedOperationId);
        when(operationService.findById(completedOperationId)).thenReturn(Optional.of(new Operation()));

        controller.completeOtkFive(operationId, "passed", form, authentication());

        verify(operationService).completeOperationWithDescription(
                operationId, account, QUALITY_CHECK_5.getCode(), "готово");
    }

    @Test
    public void otkSixPassedCompletesQualityCheckAndMarksDeviceReady() throws Exception {
        UUID operationId = UUID.randomUUID();
        UUID qualityOperationId = UUID.randomUUID();
        UUID readyOperationId = UUID.randomUUID();
        Operation form = Operation.builder().description("принято").build();
        when(operationService.completeOperationWithDescription(
                operationId, account, QUALITY_CHECK_6.getCode(), "принято"))
                .thenReturn(qualityOperationId);
        when(operationService.completeOperationWithDescription(
                qualityOperationId, account, READY.getCode(), "принято"))
                .thenReturn(readyOperationId);
        when(operationService.findById(qualityOperationId)).thenReturn(Optional.of(new Operation()));

        controller.completeOtkSix(operationId, "passed", form, authentication());

        verify(operationService).completeOperationWithDescription(
                qualityOperationId, account, READY.getCode(), "принято");
    }

    @Test
    public void otkOneThrowsWhenOperationDoesNotExist() {
        UUID operationId = UUID.randomUUID();
        when(operationService.findById(operationId)).thenReturn(Optional.empty());

        expectThrows(OperationNotFoundException.class, () -> controller.completeOtkOne(
                operationId,
                "passed",
                Operation.builder().description("проверено").build(),
                authentication()));
    }

    @DataProvider
    public Object[][] otkEndpoints() {
        return new Object[][]{
                {1},
                {2},
                {3},
                {4},
                {5},
                {6}
        };
    }

    @DataProvider
    public Object[][] otkPostMethods() {
        return new Object[][]{
                {"completeOtkOne"},
                {"completeOtkTwo"},
                {"completeOtkThree"},
                {"completeOtkFour"},
                {"completeOtkFive"},
                {"completeOtkSix"}
        };
    }

    @Test(dataProvider = "otkPostMethods")
    public void postOperationAllowsOnlyAdminAndQualityRoles(String methodName) throws Exception {
        PreAuthorize authorization = OtkController.class
                .getMethod(methodName, UUID.class, String.class, Operation.class, Authentication.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals(authorization.value(), "hasAnyRole('ADMIN', 'QUALITY')");
    }

    @Test(dataProvider = "otkEndpoints")
    public void unsupportedActionReturnsBadRequestWithoutCallingServices(int endpoint) throws Exception {
        UUID operationId = UUID.randomUUID();
        Operation form = Operation.builder().description("описание").build();

        ResponseEntity<?> response = switch (endpoint) {
            case 1 -> controller.completeOtkOne(operationId, "unsupported", form, authentication());
            case 2 -> controller.completeOtkTwo(operationId, "unsupported", form, authentication());
            case 3 -> controller.completeOtkThree(operationId, "unsupported", form, authentication());
            case 4 -> controller.completeOtkFour(operationId, "unsupported", form, authentication());
            case 5 -> controller.completeOtkFive(operationId, "unsupported", form, authentication());
            case 6 -> controller.completeOtkSix(operationId, "unsupported", form, authentication());
            default -> throw new IllegalArgumentException("Unknown endpoint: " + endpoint);
        };

        assertEquals(response.getStatusCode().value(), 400);
        verifyNoInteractions(operationService, deviceSubTypeService, operationQueryService, fileService);
        verify(accountRepository, never()).findByUsername("quality");
    }

    private Operation operationWithStatus(String status) {
        return Operation.builder()
                .status(OperationStatus.builder().name(status).build())
                .build();
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken("quality", null);
    }
}
