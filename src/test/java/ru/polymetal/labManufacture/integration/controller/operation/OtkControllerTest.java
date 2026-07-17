package ru.polymetal.labManufacture.integration.controller.operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
public class OtkControllerTest extends AbstractTestNGSpringContextTests {

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

        Role qualityRole = roleRepository.findByName("quality").orElseThrow();

        Account account = AccountTestData.createQuality(passwordEncoder, qualityRole);
        accountRepository.saveAndFlush(account);

    }

    @AfterClass
    public void deleteUser() {

        accountRepository.findByUsername("qualityTestMVC")
                .ifPresent(accountRepository::delete);
    }

    @Test
    public void testShowOtkOneDeviceForm() throws Exception {

        mockMvc.perform(get("/device/otk1-board")
                        .with(user("qualityTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/otk/otk1-board"));
    }

    @Test
    public void testCompleteOtkOne() {
    }

    @Test
    public void testShowOtkTwoDeviceForm() throws Exception {

        mockMvc.perform(get("/device/otk2-board")
                        .with(user("qualityTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/otk/otk2-board"));
    }

    @Test
    public void testCompleteOtkTwo() {
    }

    @Test
    public void testShowOtkThreeDeviceForm() throws Exception {

        mockMvc.perform(get("/device/otk3-board")
                        .with(user("qualityTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/otk/otk3-board"));
    }

    @Test
    public void testCompleteOtkThree() {
    }

    @Test
    public void testShowOtkFourDeviceForm() throws Exception {

        mockMvc.perform(get("/device/otk4-board")
                        .with(user("qualityTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/otk/otk4-board"));
    }

    @Test
    public void testCompleteOtkFour() {
    }

    @Test
    public void testShowOtkFiveDeviceForm() throws Exception {

        mockMvc.perform(get("/device/otk5-board")
                        .with(user("qualityTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/otk/otk5-board"));
    }

    @Test
    public void testCompleteOtkFive() {

    }

    @Test
    public void testShowOtkSixDeviceForm() throws Exception {

        mockMvc.perform(get("/device/otk6-board")
                        .with(user("qualityTestMVC")))
                .andExpect(status().isOk())
                .andExpect(view().name("operation/otk/otk6-board"));
    }

    @Test
    public void testCompleteOtkSix() {
    }

}
