package ru.polymetal.labManufacture.integration.controller.operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.transaction.annotation.Transactional;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.INSTALLATION;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.INSTALLATION2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_4;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_6;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.SIDE2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.TEST;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.VARNISH;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.WASHING1;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.data.repository.OperationStatusRepository;
import ru.polymetal.labManufacture.data.repository.RoleRepository;
import ru.polymetal.labManufacture.integration.testdata.AccountTestData;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class OtkControllerTest extends BaseIntegrationTest {

    @Autowired
    protected OperationStatusRepository operationStatusRepository;

    @Autowired
    private MockMvc mockMvc;


    @Test
    public void testShowOtkOneDeviceForm() throws Exception {

        mockMvc.perform(get("/device/otk1-board")
                        .with(user("adminTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/otk/otk1-board"));
    }

    @Test
    @Transactional
    public void testCompleteOtkOne() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(SIDE2)
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/otk1-board/complete")
                        .with(user("adminTestMVC"))
                        .param("deviceId", operation.getId().toString())
                        .param("action", "passed"))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> QUALITY_CHECK_1.equals(o.getStatus().getName())),
                "Не найдена операция со статусом QUALITY_CHECK_1"
        );

        operationRepository.delete(operation);
    }

    @Test
    public void testShowOtkTwoDeviceForm() throws Exception {

        mockMvc.perform(get("/device/otk2-board")
                        .with(user("adminTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/otk/otk2-board"));
    }

    @Test
    @Transactional
    public void testCompleteOtkTwo() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(INSTALLATION)
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/otk2-board/complete")
                        .with(user("adminTestMVC"))
                        .param("deviceId", operation.getId().toString())
                        .param("action", "passed"))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> QUALITY_CHECK_2.equals(o.getStatus().getName())),
                "Не найдена операция со статусом QUALITY_CHECK_2"
        );

        operationRepository.delete(operation);
    }

    @Test
    public void testShowOtkThreeDeviceForm() throws Exception {

        mockMvc.perform(get("/device/otk3-board")
                        .with(user("adminTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/otk/otk3-board"));
    }

    @Test
    @Transactional
    public void testCompleteOtkThree() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(REPAIR3)
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/otk3-board/complete")
                        .with(user("adminTestMVC"))
                        .param("deviceId", operation.getId().toString())
                        .param("action", "passed"))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> QUALITY_CHECK_3.equals(o.getStatus().getName())),
                "Не найдена операция со статусом QUALITY_CHECK_3"
        );

        operationRepository.delete(operation);
    }

    @Test
    public void testShowOtkFourDeviceForm() throws Exception {

        mockMvc.perform(get("/device/otk4-board")
                        .with(user("adminTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/otk/otk4-board"));
    }

    @Test
    @Transactional
    public void testCompleteOtkFour() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(INSTALLATION2)
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/otk4-board/complete")
                        .with(user("adminTestMVC"))
                        .param("deviceId", operation.getId().toString())
                        .param("action", "passed"))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> QUALITY_CHECK_4.equals(o.getStatus().getName())),
                "Не найдена операция со статусом QUALITY_CHECK_4"
        );

        operationRepository.delete(operation);
    }

    @Test
    public void testShowOtkFiveDeviceForm() throws Exception {

        mockMvc.perform(get("/device/otk5-board")
                        .with(user("adminTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/otk/otk5-board"));
    }

    @Test
    @Transactional
    public void testCompleteOtkFive() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(WASHING1)
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/otk5-board/complete")
                        .with(user("adminTestMVC"))
                        .param("deviceId", operation.getId().toString())
                        .param("action", "passed"))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> QUALITY_CHECK_5.equals(o.getStatus().getName())),
                "Не найдена операция со статусом QUALITY_CHECK_5"
        );

        operationRepository.delete(operation);

    }

    @Test
    public void testShowOtkSixDeviceForm() throws Exception {

        mockMvc.perform(get("/device/otk6-board")
                        .with(user("adminTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/otk/otk6-board"));
    }

    @Test
    @Transactional
    public void testCompleteOtkSix() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(VARNISH)
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/otk6-board/complete")
                        .with(user("adminTestMVC"))
                        .param("deviceId", operation.getId().toString())
                        .param("action", "passed"))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> QUALITY_CHECK_6.equals(o.getStatus().getName())),
                "Не найдена операция со статусом QUALITY_CHECK_6"
        );

        operationRepository.delete(operation);

    }
}
