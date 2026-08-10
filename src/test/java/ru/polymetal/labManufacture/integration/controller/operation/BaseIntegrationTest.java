package ru.polymetal.labManufacture.integration.controller.operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.DeviceType;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.data.repository.DeviceRepository;
import ru.polymetal.labManufacture.data.repository.DeviceSubTypeRepository;
import ru.polymetal.labManufacture.data.repository.DeviceTypeRepository;
import ru.polymetal.labManufacture.data.repository.OperationRepository;
import ru.polymetal.labManufacture.data.repository.RoleRepository;
import ru.polymetal.labManufacture.integration.testdata.AccountTestData;
import ru.polymetal.labManufacture.integration.testdata.DeviceSubTypeData;
import ru.polymetal.labManufacture.integration.testdata.DeviceTestData;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected DeviceTypeRepository deviceTypeRepository;

    @Autowired
    protected DeviceSubTypeRepository deviceSubTypeRepository;

    @Autowired
    protected DeviceRepository deviceRepository;

    @Autowired
    protected OperationRepository operationRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeClass
    public void prepareTestData() {

        Role operatorRole = roleRepository.findByName("admin")
                .orElseThrow();

        if (accountRepository.findByUsername("adminTestMVC").isEmpty()) {
            Account account =
                    AccountTestData.createAdmin(passwordEncoder, operatorRole);

            accountRepository.saveAndFlush(account);
        }

        DeviceType deviceType = deviceTypeRepository.findByName("BOARD")
                .orElseThrow(() -> new RuntimeException("Тип не найден"));

        DeviceSubType deviceSubType =
                deviceSubTypeRepository.findAllByName("forTest")
                        .stream()
                        .findFirst()
                        .orElseGet(() -> {
                            DeviceSubType newSubType =
                                    DeviceSubTypeData.createDeviceSubTypeData("forTest");

                            return deviceSubTypeRepository.save(newSubType);
                        });

        if (deviceRepository.findBySerialNumber("forTest").isEmpty()) {

            Device device = DeviceTestData.createDevice(
                    "forTest",
                    deviceType,
                    deviceSubType
            );

            deviceRepository.saveAndFlush(device);
        }
    }

    @AfterClass
    public void cleanupTestData() {

        accountRepository.findByUsername("adminTestMVC")
                .ifPresent(account -> {

                    List<Operation> operations =
                            operationRepository.findByAccountId(account.getId());

                    operationRepository.deleteAll(operations);

                    accountRepository.delete(account);
                });

        deviceRepository.findBySerialNumber("forTest")
                .ifPresent(deviceRepository::delete);

        deviceSubTypeRepository.deleteAll(
                deviceSubTypeRepository.findAllByName("forTest")
        );
    }
}
