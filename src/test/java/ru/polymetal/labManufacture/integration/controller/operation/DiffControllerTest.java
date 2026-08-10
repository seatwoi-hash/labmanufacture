package ru.polymetal.labManufacture.integration.controller.operation;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.CREATE;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.SIDE1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.SIDE2;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.DeviceType;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.data.repository.DeviceRepository;
import ru.polymetal.labManufacture.data.repository.DeviceSubTypeRepository;
import ru.polymetal.labManufacture.data.repository.DeviceTypeRepository;
import ru.polymetal.labManufacture.data.repository.OperationRepository;
import ru.polymetal.labManufacture.data.repository.OperationStatusRepository;
import ru.polymetal.labManufacture.data.repository.RoleRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.testng.Assert.assertTrue;
import ru.polymetal.labManufacture.integration.testdata.AccountTestData;
import ru.polymetal.labManufacture.integration.testdata.DeviceSubTypeData;
import ru.polymetal.labManufacture.integration.testdata.DeviceTestData;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
public class DiffControllerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected DeviceSubTypeRepository deviceSubTypeRepository;

    @Autowired
    protected DeviceTypeRepository deviceTypeRepository;

    @Autowired
    protected DeviceRepository deviceRepository;

    @Autowired
    protected OperationRepository operationRepository;

    @Autowired
    protected OperationStatusRepository operationStatusRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @BeforeClass
    public void createUser() {

        Role operatorRole = roleRepository.findByName("operator").orElseThrow();

        Account account = AccountTestData.createOperator(passwordEncoder, operatorRole);
        accountRepository.saveAndFlush(account);

        DeviceType deviceType = deviceTypeRepository.findByName("BOARD").orElseThrow(() -> new RuntimeException("тип " +
                "не найден"));

        DeviceSubType deviceSubType = DeviceSubTypeData.createDeviceSubTypeData("forTest");
        deviceSubTypeRepository.save(deviceSubType);
        Device device = DeviceTestData.createDevice("forTest", deviceType, deviceSubType);
        deviceRepository.save(device);

    }

    @AfterClass
    public void deleteUser() {

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("operatorTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        operationRepository.deleteAll(operations);

        accountRepository.findByUsername("operatorTestMVC")
                .ifPresent(accountRepository::delete);

        deviceSubTypeRepository.deleteAll(
                deviceSubTypeRepository.findAllByName("forTest"));

        deviceRepository.findBySerialNumber("forTest")
                .ifPresent(deviceRepository::delete);
    }

    @Test
    public void showMOneDeviceForm() throws Exception {

        mockMvc.perform(get("/device/mone-board")
                        .with(user("operatorTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/board/mone-board"));
    }

    @Test
    @Transactional
    public void completeMOne() throws Exception {

        OperationStatus operationStatus = operationStatusRepository.findByName(CREATE)
                .orElseThrow(() -> new RuntimeException("Статус не найден"));

        Operation operation = operationRepository.findByStatusIdAndDeletedWithFetch(operationStatus.getId()).get(0);
        // UUID

        mockMvc.perform(post("/device/mone-board/complete")
                        .with(user("operatorTestMVC"))
                        .param("deviceId", operation.getId().toString()))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("operatorTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> SIDE1.equals(o.getStatus().getName())),
                "Не найдена операция со статусом SIDE1"
        );
    }

    @Test
    public void showMTwoDeviceForm() throws Exception {

        mockMvc.perform(get("/device/mtwo-board")
                        .with(user("operatorTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/board/mtwo-board"));
    }

    @Test
    @Transactional
    void completeMTwo() throws Exception {

        OperationStatus operationStatus = operationStatusRepository.findByName(CREATE)
                .orElseThrow(() -> new RuntimeException("Статус не найден"));

        Operation operation = operationRepository.findByStatusIdAndDeletedWithFetch(operationStatus.getId()).get(0);
        // UUID

        mockMvc.perform(post("/device/mtwo-board/complete")
                        .with(user("operatorTestMVC"))
                        .param("deviceId", operation.getId().toString()))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("operatorTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> SIDE2.equals(o.getStatus().getName())),
                "Не найдена операция со статусом SIDE2"
        );
    }

    @Test
    void showReadyBoard() throws Exception {

        mockMvc.perform(get("/device/ready-board")
                        .with(user("operatorTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("board/ready-board"));
    }

    @Test
    void showNoReadyBoard() throws Exception {

        mockMvc.perform(get("/device/noready-board")
                        .with(user("operatorTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("board/noready-board"));
    }

    @Test
    void showOperationBoard() throws Exception {

        mockMvc.perform(get("/device/operation-board/forTest")
                .with(user("operatorTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("board/all-operation-board"));

    }
}
