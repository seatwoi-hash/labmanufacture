package ru.polymetal.labManufacture.integration.controller.operation;

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
import ru.polymetal.labManufacture.integration.testdata.AccountTestData;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
public class RepairControllerTest extends AbstractTestNGSpringContextTests {

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

        Role repairmanRole = roleRepository.findByName("repairman").orElseThrow();

        Account account = AccountTestData.createRepairman(passwordEncoder, repairmanRole);
        accountRepository.saveAndFlush(account);
    }

    @AfterClass
    public void deleteUser() {

        accountRepository.findByUsername("repairmanTestMVC")
                .ifPresent(accountRepository::delete);
    }

    @Test
    public void testShowRepairOneDeviceForm() throws Exception {

        mockMvc.perform(get("/device/repair1-board")
                        .with(user("repairmanTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/repair/repair1-board"));
    }

    @Test
    public void testCompleteRepairOne() {
    }

    @Test
    public void testShowRepairTwoDeviceForm() throws Exception {

        mockMvc.perform(get("/device/repair2-board")
                        .with(user("repairmanTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/repair/repair2-board"));
    }

    @Test
    public void testCompleteRepairTwo() {
    }

    @Test
    public void testShowRepairThreeDeviceForm() throws Exception {

        mockMvc.perform(get("/device/repair3-board")
                        .with(user("repairmanTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/repair/repair3-board"));
    }

    @Test
    public void testCompleteRepairThree() {
    }

    @Test
    public void testShowRepairFourDeviceForm() throws Exception {

        mockMvc.perform(get("/device/repair4-board")
                        .with(user("repairmanTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/repair/repair4-board"));
    }

    @Test
    public void testCompleteRepairFour() {
    }

    @Test
    public void testShowRepairFiveDeviceForm() throws Exception {

        mockMvc.perform(get("/device/repair5-board")
                        .with(user("repairmanTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/repair/repair5-board"));
    }

    @Test
    public void testCompleteRepairFive() {
    }

    @Test
    public void testShowRepairSixDeviceForm() throws Exception {

        mockMvc.perform(get("/device/repair6-board")
                        .with(user("repairmanTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/repair/repair6-board"));
    }

    @Test
    public void testCompleteRepairSix() {
    }

}
