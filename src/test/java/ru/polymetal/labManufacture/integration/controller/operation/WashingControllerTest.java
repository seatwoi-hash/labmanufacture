package ru.polymetal.labManufacture.integration.controller.operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.data.repository.RoleRepository;
import ru.polymetal.labManufacture.integration.testdata.AccountTestData;

@SpringBootTest
@AutoConfigureMockMvc
public class WashingControllerTest extends AbstractTestNGSpringContextTests {

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

        Role washerRole = roleRepository.findByName("washer").orElseThrow();

        Account account = AccountTestData.createWasher(passwordEncoder, washerRole);
        accountRepository.saveAndFlush(account);

    }

    @AfterClass
    public void deleteUser() {

        accountRepository.findByUsername("washerTestMVC")
                .ifPresent(accountRepository::delete);
    }

    @Test
    public void testShowWashingOneDeviceForm() throws Exception {

        mockMvc.perform(get("/device/washing1-board")
                        .with(user("washerTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/washing/washing1-board"));
    }

    @Test
    public void testCompleteWashingOne() {
    }

    @Test
    public void testShowWashingTwoDeviceForm() throws Exception {

        mockMvc.perform(get("/device/washing2-board")
                        .with(user("washerTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/washing/washing2-board"));
    }

    @Test
    public void testCompleteWashingTwo() {
    }

}
