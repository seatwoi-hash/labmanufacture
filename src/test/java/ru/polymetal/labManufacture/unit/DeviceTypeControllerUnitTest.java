package ru.polymetal.labManufacture.unit;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.controller.devicetype.DeviceTypeController;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.account.AccountService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class DeviceTypeControllerUnitTest {

    private MockMvc mockMvc;
    private AutoCloseable mocks;

    @Mock
    private DeviceSubTypeService deviceSubTypeService;
    @Mock
    private AccountService accountService;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new DeviceTypeController(deviceSubTypeService, accountService)
        ).build();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void showCreateFormPopulatesRequiredModel() throws Exception {
        Account account = new Account();
        account.setUsername("developer");
        when(accountService.findByUsername("developer")).thenReturn(account);
        when(deviceSubTypeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/devicetype/add").principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(view().name("usermenu/add-type-devices"))
                .andExpect(model().attribute("currentUser", account))
                .andExpect(model().attributeExists("subtype", "subtypeList"));
    }

    @Test
    public void invalidCreateFormIsNotSaved() throws Exception {
        Account account = new Account();
        when(accountService.findByUsername("developer")).thenReturn(account);
        when(deviceSubTypeService.findAll()).thenReturn(List.of());

        mockMvc.perform(multipart("/devicetype/add")
                        .param("name", "")
                        .param("description", "")
                        .param("snType", "9")
                        .param("versionType", "10")
                        .principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(view().name("usermenu/add-type-devices"))
                .andExpect(model().attributeHasFieldErrors("subtype", "name", "description", "snType", "versionType"));

        verify(deviceSubTypeService, never()).save(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void downloadUsesEncodedSafeFilename() throws Exception {
        UUID id = UUID.randomUUID();
        DeviceSubType subtype = DeviceSubType.builder()
                .id(id).name("Плата\"тест").archiveOriginalName("project.tar.gz")
                .data(new byte[]{1, 2, 3}).build();
        when(deviceSubTypeService.findById(id)).thenReturn(Optional.of(subtype));

        mockMvc.perform(get("/devicetype/{id}/download", id))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Length", "3"))
                .andExpect(header().string("Content-Disposition", containsString("altium-")))
                .andExpect(header().string("Content-Disposition", containsString(".tar.gz")));
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken("developer", null);
    }
}
