package ru.polymetal.labManufacture.unit;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.constant.DeviceStatusCodes;
import ru.polymetal.labManufacture.controller.operation.DiffController;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.device.DeviceService;
import ru.polymetal.labManufacture.service.operation.OperationQueryService;
import ru.polymetal.labManufacture.service.operation.OperationRollbackService;
import ru.polymetal.labManufacture.service.operation.OperationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
public class DiffControllerUnitTest {

    private MockMvc mockMvc;

    private AutoCloseable mocks;

    @Mock
    private DeviceService deviceService;

    @Mock
    private OperationService operationService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private DeviceSubTypeService deviceSubTypeService;

    @Mock
    private DeviceStatusService deviceStatusService;

    @Mock
    private OperationQueryService operationQueryService;

    @Mock
    private OperationRollbackService operationRollbackService;


    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        DiffController controller = new DiffController(
                accountRepository,
                operationService,
                deviceStatusService,
                deviceSubTypeService,
                deviceService,
                operationQueryService,
                operationRollbackService
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void showMOneDeviceForm() throws Exception {
        OperationStatus operationStatus = OperationStatus.builder()
                .id(UUID.randomUUID())
                .devices(Set.of())
                .name("CREATE")
                .build();


        List<Operation> operations = List.of(
                new Operation(),
                new Operation()
        );

        Account account = new Account();
        account.setUsername("operatorTestMVC");

        when(operationQueryService.findOperationsByStatusNames(Set.of("CREATE")))
                .thenReturn(operations);

        when(accountRepository.findByUsername("operatorTestMVC"))
                .thenReturn(Optional.of(account));


        mockMvc.perform(get("/device/mone-board")
                        .principal(new UsernamePasswordAuthenticationToken(
                                "operatorTestMVC",
                                null
                        )))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/board/mone-board"));

        verify(accountRepository)
                .findByUsername("operatorTestMVC");
    }

    @Test
    public void returnedReadyDeviceAppearsInProductionList() throws Exception {
        OperationStatus readyStatus = OperationStatus.builder()
                .name(DeviceStatusCodes.READY.getCode())
                .build();
        OperationStatus productionStatus = OperationStatus.builder()
                .name(DeviceStatusCodes.SIDE1.getCode())
                .build();

        Operation historicalReady = Operation.builder()
                .status(readyStatus)
                .isDeleted(true)
                .build();
        Operation activeProduction = Operation.builder()
                .status(productionStatus)
                .isDeleted(false)
                .build();
        Device returnedDevice = Device.builder()
                .id(UUID.randomUUID())
                .serialNumber("RETURNED-001")
                .isDeleted(false)
                .operations(List.of(activeProduction, historicalReady))
                .build();

        Account account = Account.builder().username("admin").build();
        when(deviceService.findAll()).thenReturn(List.of(returnedDevice));
        when(accountRepository.findByUsername("admin")).thenReturn(Optional.of(account));

        mockMvc.perform(get("/device/noready-board")
                        .principal(new UsernamePasswordAuthenticationToken("admin", null)))
                .andExpect(status().isOk())
                .andExpect(view().name("board/noready-board"))
                .andExpect(model().attribute("devices", List.of(returnedDevice)));
    }

    @Test
    public void deviceDisplayStatusSkipsAllTechnicalOperations() {
        OperationStatus technical = OperationStatus.builder()
                .name(DeviceStatusCodes.TECHNICAL.getCode())
                .build();
        OperationStatus technicalTwo = OperationStatus.builder()
                .name(DeviceStatusCodes.TECHNICAL2.getCode())
                .build();
        OperationStatus sideOne = OperationStatus.builder()
                .name(DeviceStatusCodes.SIDE1.getCode())
                .build();
        Operation latestTechnical = Operation.builder().status(technical).build();
        Operation previousTechnical = Operation.builder().status(technicalTwo).build();
        Operation productionOperation = Operation.builder().status(sideOne).build();
        Device device = Device.builder()
                .operations(List.of(latestTechnical, previousTechnical, productionOperation))
                .build();

        org.testng.Assert.assertSame(device.getDisplayStatus(), productionOperation);
    }

    @Test
    public void productionBoardShowsOperationBeforeActiveTechnicalStatus() throws Exception {
        UUID deviceId = UUID.randomUUID();
        OperationStatus technical = OperationStatus.builder()
                .name(DeviceStatusCodes.TECHNICAL3.getCode())
                .build();
        Operation activeTechnical = Operation.builder()
                .status(technical)
                .isDeleted(false)
                .build();
        Device device = Device.builder()
                .id(deviceId)
                .serialNumber("TECHNICAL-001")
                .isDeleted(false)
                .operations(List.of(activeTechnical))
                .build();
        Account account = Account.builder().username("admin").build();

        when(deviceService.findAll()).thenReturn(List.of(device));
        when(operationService.getPreviousStatus(technical))
                .thenReturn("Монтаж \"Сторона 1\"");
        when(accountRepository.findByUsername("admin")).thenReturn(Optional.of(account));

        mockMvc.perform(get("/device/noready-board")
                        .principal(new UsernamePasswordAuthenticationToken("admin", null)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("deviceDisplayNames", Map.of(
                        deviceId, "Монтаж \"Сторона 1\"")));
    }

    @Test
    public void operationBoardShowsTechnicalRollbackButHidesRegularTechnicalOperation() throws Exception {
        Operation regularTechnical = Operation.builder()
                .status(OperationStatus.builder().name(DeviceStatusCodes.TECHNICAL.getCode()).build())
                .createdTime(LocalDateTime.of(2026, 8, 30, 10, 0))
                .isRollback(false)
                .build();
        Operation productionOperation = Operation.builder()
                .id(UUID.randomUUID())
                .status(OperationStatus.builder()
                        .name(DeviceStatusCodes.SIDE1.getCode())
                        .description("Монтаж \"Сторона 1\"")
                        .build())
                .createdTime(LocalDateTime.of(2026, 8, 30, 11, 0))
                .isRollback(false)
                .build();
        Operation technicalRollback = Operation.builder()
                .id(UUID.randomUUID())
                .status(OperationStatus.builder().name(DeviceStatusCodes.TECHNICAL2.getCode()).build())
                .createdTime(LocalDateTime.of(2026, 8, 30, 12, 0))
                .isRollback(true)
                .build();
        Account account = Account.builder().username("admin").build();

        when(operationService.findBySerialNumber("RETURNED-002"))
                .thenReturn(List.of(technicalRollback, regularTechnical, productionOperation));
        when(operationService.getPreviousStatus(technicalRollback.getStatus()))
                .thenReturn("Тестировка №2");
        when(accountRepository.findByUsername("admin")).thenReturn(Optional.of(account));

        mockMvc.perform(get("/device/operation-board/RETURNED-002")
                        .principal(new UsernamePasswordAuthenticationToken("admin", null)))
                .andExpect(status().isOk())
                .andExpect(view().name("board/all-operation-board"))
                .andExpect(model().attribute(
                        "operations", List.of(productionOperation, technicalRollback)))
                .andExpect(model().attribute("operationDisplayNames", Map.of(
                        productionOperation.getId(), productionOperation.getStatus().getDescription(),
                        technicalRollback.getId(), "Тестировка №2")));
    }

    @Test
    public void operationBoardExposesCancellationOnlyForEligibleLastOperation() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();
        Account account = Account.builder().id(accountId).username("operator").build();
        Operation operation = Operation.builder()
                .id(operationId)
                .device(Device.builder().id(UUID.randomUUID()).serialNumber("CANCEL-001").build())
                .status(OperationStatus.builder().name("Side2").description("Сторона 2").build())
                .account(account)
                .createdTime(LocalDateTime.of(2026, 8, 31, 11, 50))
                .isRollback(false)
                .build();

        when(operationService.findBySerialNumber("CANCEL-001")).thenReturn(List.of(operation));
        when(accountRepository.findByUsername("operator")).thenReturn(Optional.of(account));
        when(operationRollbackService.canCancelOwnLastOperation(operationId, account)).thenReturn(true);

        mockMvc.perform(get("/device/operation-board/CANCEL-001")
                        .principal(new UsernamePasswordAuthenticationToken("operator", null)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cancelableOperationId", operationId));
    }

    @Test
    public void cancelOperationDelegatesToRollbackServiceAndRedirectsToHistory() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();
        Account account = Account.builder().id(accountId).username("operator").build();
        when(accountRepository.findByUsername("operator")).thenReturn(Optional.of(account));

        mockMvc.perform(post("/device/operation-board/CANCEL-001/cancel")
                        .param("operationId", operationId.toString())
                        .param("comment", "Ошибка проверки")
                        .principal(new UsernamePasswordAuthenticationToken("operator", null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/device/operation-board/CANCEL-001"))
                .andExpect(flash().attribute("success", "Операция успешно отменена"));

        verify(operationRollbackService).cancelOwnLastOperation(
                operationId, account, "Ошибка проверки");
    }

}
