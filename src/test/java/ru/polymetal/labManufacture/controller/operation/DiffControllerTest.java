package ru.polymetal.labManufacture.controller.operation;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.data.repository.RoleRepository;
import testdata.AccountTestData;


@SpringBootTest
@AutoConfigureMockMvc
public class DiffControllerTest  extends AbstractTestNGSpringContextTests {

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @BeforeClass
    public void createUser() {

        Role qualityRole = roleRepository.findByName("operator").orElseThrow();

        Account account = AccountTestData.createOperator(passwordEncoder, qualityRole);
        accountRepository.saveAndFlush(account);

    }

    @AfterClass
    public void deleteUser() {

        accountRepository.findByUsername("operatorTestMVC")
                .ifPresent(accountRepository::delete);
    }

    @Test
    public void showMOneDeviceForm() throws Exception {

        mockMvc.perform(get("/device/mone-board")
                    .with(user("operatorTestMVC")))
                    .andExpect(status().isOk())
                    .andExpect(view().name("operation/board/mone-board"));
    }

    @Test
    public void completeMOne() {
    }

    @Test
    public void showMTwoDeviceForm() throws Exception {

        mockMvc.perform(get("/device/mtwo-board")
                        .with(user("operatorTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/board/mtwo-board"));
    }

    @Test
    void completeMTwo() {
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

//        mockMvc.perform(get("/device/operation-board/{sn}"))  TODO: test data sn board create!!!
    }

}
