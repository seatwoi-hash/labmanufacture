package ru.polymetal.labManufacture.controller.operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.data.repository.RoleRepository;
import testdata.AccountTestData;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
public class TestControllerTest extends AbstractTestNGSpringContextTests {

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

        Role testerBRole = roleRepository.findByName("testerB").orElseThrow();

        Account account = AccountTestData.createTesterB(passwordEncoder, testerBRole);
        accountRepository.saveAndFlush(account);

    }

    @AfterClass
    public void deleteUser() {

        accountRepository.findByUsername("testerBTestMVC")
                .ifPresent(accountRepository::delete);
    }

    @Test
    public void testShowTestDeviceForm() throws Exception {

        mockMvc.perform(get("/device/test-board")
                        .with(user("testerBTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/test/test-board"));
    }

    @Test
    public void testCompleteTest() {
    }

    @Test
    public void testShowTestTwoDeviceForm() throws Exception {

        mockMvc.perform(get("/device/test-board-two")
                        .with(user("testerBTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/test/test2-board"));
    }

    @Test
    public void testCompleteTestTwo() {
    }

}
