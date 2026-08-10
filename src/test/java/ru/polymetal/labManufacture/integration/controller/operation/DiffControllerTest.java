package ru.polymetal.labManufacture.integration.controller.operation;

import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.CREATE;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.SIDE1;
import static ru.polymetal.labManufacture.constant.DeviceStatusCodes.SIDE2;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.repository.OperationStatusRepository;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.testng.Assert.assertTrue;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class DiffControllerTest extends BaseIntegrationTest {

    @Autowired
    protected OperationStatusRepository operationStatusRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void showMOneDeviceForm() throws Exception {

        mockMvc.perform(get("/device/mone-board")
                        .with(user("adminTestMVC")))
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
                        .with(user("adminTestMVC"))
                        .param("deviceId", operation.getId().toString()))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
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
                        .with(user("adminTestMVC")))
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
                        .with(user("adminTestMVC"))
                        .param("deviceId", operation.getId().toString()))
                .andExpect(status().isOk());

        List<Operation> operations =
                operationRepository.findByAccountId(accountRepository.findByUsername("adminTestMVC")
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
                        .with(user("adminTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("board/ready-board"));
    }

    @Test
    void showNoReadyBoard() throws Exception {

        mockMvc.perform(get("/device/noready-board")
                        .with(user("adminTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("board/noready-board"));
    }

    @Test
    void showOperationBoard() throws Exception {

        mockMvc.perform(get("/device/operation-board/forTest")
                .with(user("adminTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("board/all-operation-board"));

    }
}
