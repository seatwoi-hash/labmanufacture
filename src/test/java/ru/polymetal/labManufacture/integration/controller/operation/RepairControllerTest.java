package ru.polymetal.labManufacture.integration.controller.operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.transaction.annotation.Transactional;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.DIAGNOSTICIAN_REPAIR_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.DIAGNOSTICIAN_REPAIR_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_4;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_QUALITY_CHECK_5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.QUALITY_CHECK_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR3;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR4;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR5;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.REPAIR6;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.SIDE2;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class RepairControllerTest  extends BaseIntegrationTest {

    @Autowired
    protected OperationStatusRepository operationStatusRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testShowRepairOneDeviceForm() throws Exception {

        mockMvc.perform(get("/device/repair1-board")
                        .with(user("adminTestMVC").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/repair/repair1-board"));
    }

    @Test
    @Transactional
    public void testCompleteRepairOne() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(FAIL_QUALITY_CHECK_1.getCode())
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/repair1-board/complete")
                        .with(user("adminTestMVC").roles("ADMIN"))
                        .param("deviceId", operation.getId().toString()))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> REPAIR1.getCode().equals(o.getStatus().getName())),
                "Не найдена операция со статусом REPAIR1"
        );

        operationRepository.delete(operation);
    }

    @Test
    public void testShowRepairTwoDeviceForm() throws Exception {

        mockMvc.perform(get("/device/repair2-board")
                        .with(user("adminTestMVC").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/repair/repair2-board"));
    }

    @Test
    @Transactional
    public void testCompleteRepairTwo() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(FAIL_QUALITY_CHECK_2.getCode())
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/repair2-board/complete")
                        .with(user("adminTestMVC").roles("ADMIN"))
                        .param("deviceId", operation.getId().toString()))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> REPAIR2.getCode().equals(o.getStatus().getName())),
                "Не найдена операция со статусом REPAIR2"
        );

        operationRepository.delete(operation);
    }

    @Test
    public void testShowRepairThreeDeviceForm() throws Exception {

        mockMvc.perform(get("/device/repair3-board")
                    .with(user("adminTestMVC").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/repair/repair3-board"));
    }

    @Test
    @Transactional
    public void testCompleteRepairThree() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(DIAGNOSTICIAN_REPAIR_1.getCode())
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/repair3-board/complete")
                        .with(user("adminTestMVC").roles("ADMIN"))
                        .param("deviceId", operation.getId().toString()))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> REPAIR3.getCode().equals(o.getStatus().getName())),
                "Не найдена операция со статусом REPAIR3"
        );

        operationRepository.delete(operation);
    }

    @Test
    public void testShowRepairFourDeviceForm() throws Exception {

        mockMvc.perform(get("/device/repair4-board")
                    .with(user("adminTestMVC").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/repair/repair4-board"));
    }

    @Test
    @Transactional
    public void testCompleteRepairFour() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(FAIL_QUALITY_CHECK_4.getCode())
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/repair4-board/complete")
                        .with(user("adminTestMVC").roles("ADMIN"))
                        .param("deviceId", operation.getId().toString()))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> REPAIR4.getCode().equals(o.getStatus().getName())),
                "Не найдена операция со статусом REPAIR4"
        );

        operationRepository.delete(operation);
    }

    @Test
    public void testShowRepairFiveDeviceForm() throws Exception {

        mockMvc.perform(get("/device/repair5-board")
                    .with(user("adminTestMVC").roles("ADMIN")))
                .andExpect(view().name("operation/repair/repair5-board"));
    }

    @Test
    @Transactional
    public void testCompleteRepairFive() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(DIAGNOSTICIAN_REPAIR_2.getCode())
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/repair5-board/complete")
                        .with(user("adminTestMVC").roles("ADMIN"))
                        .param("deviceId", operation.getId().toString()))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> REPAIR5.getCode().equals(o.getStatus().getName())),
                "Не найдена операция со статусом REPAIR5"
        );

        operationRepository.delete(operation);
    }

    @Test
    public void testShowRepairSixDeviceForm() throws Exception {

        mockMvc.perform(get("/device/repair6-board")
                    .with(user("adminTestMVC").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/repair/repair6-board"));
    }

    @Test
    @Transactional
    public void testCompleteRepairSix() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(FAIL_QUALITY_CHECK_5.getCode())
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/repair6-board/complete")
                        .with(user("adminTestMVC").roles("ADMIN"))
                        .param("deviceId", operation.getId().toString()))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> REPAIR6.getCode().equals(o.getStatus().getName())),
                "Не найдена операция со статусом REPAIR6"
        );

        operationRepository.delete(operation);
    }

}
