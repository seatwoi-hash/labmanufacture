package ru.polymetal.labManufacture.integration.controller.operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.transaction.annotation.Transactional;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_5_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TEST_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.VARNISH;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.WASHING1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.WASHING2;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.data.repository.OperationStatusRepository;
import ru.polymetal.labManufacture.data.repository.RoleRepository;
import ru.polymetal.labManufacture.integration.testdata.AccountTestData;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class WashingControllerTest extends BaseIntegrationTest {

    @Autowired
    protected OperationStatusRepository operationStatusRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testShowWashingOneDeviceForm() throws Exception {

        mockMvc.perform(get("/device/washing1-board")
                        .with(user("adminTestMVC").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/washing/washing1-board"));
    }

    @Test
    @Transactional
    public void testCompleteWashingOne() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(TEST_2.getCode())
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/washing1-board/complete")
                        .with(user("adminTestMVC").roles("ADMIN"))
                        .param("deviceId", operation.getId().toString()))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> WASHING1.getCode().equals(o.getStatus().getName())),
                "Не найдена операция со статусом WASHING1"
        );

        operationRepository.delete(operation);
    }

    @Test
    public void testShowWashingTwoDeviceForm() throws Exception {

        mockMvc.perform(get("/device/washing2-board")
                        .with(user("adminTestMVC").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/washing/washing2-board"));
    }

    @Test
    @Transactional
    public void testCompleteWashingTwo() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(QUALITY_CHECK_5_1.getCode())
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/washing2-board/complete")
                        .with(user("adminTestMVC").roles("ADMIN"))
                        .param("deviceId", operation.getId().toString()))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> WASHING2.getCode().equals(o.getStatus().getName())),
                "Не найдена операция со статусом WASHING2"
        );

        operationRepository.delete(operation);
    }

}
