package ru.polymetal.labManufacture.unit;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.controller.RollbackOtkController;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.service.operation.OperationRollbackService;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit-тесты универсального контроллера возврата операций.
 *
 * @author Tatarinov Anton
 */
public class RollbackOtkControllerUnitTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OperationRollbackService rollbackService;

    private AutoCloseable mocks;
    private MockMvc mockMvc;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new RollbackOtkController(accountRepository, rollbackService)).build();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @DataProvider
    public Object[][] rollbackUrls() {
        return new Object[][]{
                {"/device/operations/rollback"},
                {"/device/otk1-board/rollback"},
                {"/device/otk6-board/rollback"}
        };
    }

    @Test(dataProvider = "rollbackUrls")
    public void rollbackSupportsGenericAndLegacyUrls(String url) throws Exception {
        UUID operationId = UUID.randomUUID();
        Account account = Account.builder().username("quality").build();
        when(accountRepository.findByUsername("quality")).thenReturn(Optional.of(account));

        mockMvc.perform(post(url)
                        .param("deviceId", operationId.toString())
                        .param("description", "ошибка")
                        .principal(new UsernamePasswordAuthenticationToken("quality", null)))
                .andExpect(status().isOk());

        verify(rollbackService).rollback(operationId, account, "ошибка");
    }
}
