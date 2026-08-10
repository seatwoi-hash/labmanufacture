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
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.device.DeviceService;
import ru.polymetal.labManufacture.service.operation.OperationQueryService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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


    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        DiffController controller = new DiffController(
                accountRepository,
                operationService,
                deviceStatusService,
                deviceSubTypeService,
                deviceService,
                operationQueryService
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

}
