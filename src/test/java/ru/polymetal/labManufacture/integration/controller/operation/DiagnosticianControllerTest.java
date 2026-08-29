package ru.polymetal.labManufacture.integration.controller.operation;

import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.transaction.annotation.Transactional;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.DIAGNOSTICIAN_REPAIR_1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.DIAGNOSTICIAN_REPAIR_2;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_TEST;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.FAIL_TEST_2;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.repository.OperationStatusRepository;
import java.util.List;

public class DiagnosticianControllerTest extends BaseIntegrationTest {

    @Autowired
    protected OperationStatusRepository operationStatusRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void showDiagnosticianOneTest() throws Exception {

        mockMvc.perform(get("/device/diagnostician-one")
                .with(user("adminTestMVC").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/diagnostician/diagnostician-one"));
    }

    @Test
    public void showDiagnosticianTwoTest() throws Exception {

        mockMvc.perform(get("/device/diagnostician-two")
                .with(user("adminTestMVC").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/diagnostician/diagnostician-two"));
    }

    @Test
    @Transactional
    public void completeDiagnosticianOneTest() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(FAIL_TEST.getCode())
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);

        mockMvc.perform(post("/device/diagnostician-one/complete")
                        .with(user("adminTestMVC").roles("ADMIN"))
                        .param("deviceId", operation.getId().toString())
                        .param("action", "repair"))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> DIAGNOSTICIAN_REPAIR_1.getCode().equals(o.getStatus().getName())),
                "Не найдена операция со статусом DIAGNOSTICIAN_REPAIR_1"
        );

        operationRepository.delete(operation);
    }

    @Test
    @Transactional
    public void completeDiagnosticianTwoTest() throws Exception {

        Account account = accountRepository.findByUsername("adminTestMVC")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Device device = deviceRepository.findBySerialNumber("forTest")
                .orElseThrow(() -> new RuntimeException("Device не найден"));

        OperationStatus operationStatus = operationStatusRepository.findByName(FAIL_TEST_2.getCode())
                .orElseThrow(() -> new RuntimeException("OperationStatus не найден"));

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setDevice(device);
        operation.setStatus(operationStatus);
        operationRepository.saveAndFlush(operation);


        mockMvc.perform(post("/device/diagnostician-two/complete")
                        .with(user("adminTestMVC").roles("ADMIN"))
                        .param("deviceId", operation.getId().toString())
                        .param("action", "repair"))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден")).getId());

        assertTrue(
                operations.stream()
                        .anyMatch(o -> DIAGNOSTICIAN_REPAIR_2.getCode().equals(o.getStatus().getName())),
                "Не найдена операция со статусом DIAGNOSTICIAN_REPAIR_2"
        );

        operationRepository.delete(operation);

    }

}
