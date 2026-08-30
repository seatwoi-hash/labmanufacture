package ru.polymetal.labManufacture.unit;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.data.repository.DeviceRepository;
import ru.polymetal.labManufacture.data.repository.OperationStatusRepository;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.DeviceTypeService;
import ru.polymetal.labManufacture.service.device.Impl.DeviceServiceImpl;
import ru.polymetal.labManufacture.service.nextcloud.LinkService;
import ru.polymetal.labManufacture.service.nextcloud.NextcloudService;
import ru.polymetal.labManufacture.service.operation.OperationService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit-тесты управления устройствами.
 *
 * @author Tatarinov Anton
 */
public class DeviceServiceUnitTest {

    @Mock private DeviceRepository deviceRepository;
    @Mock private OperationService operationService;
    @Mock private AccountRepository accountRepository;
    @Mock private DeviceTypeService deviceTypeService;
    @Mock private DeviceSubTypeService deviceSubTypeService;
    @Mock private OperationStatusRepository operationStatusRepository;
    @Mock private NextcloudService nextcloudService;
    @Mock private LinkService linkService;

    private AutoCloseable mocks;
    private DeviceServiceImpl service;
    private Clock clock;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        clock = Clock.fixed(Instant.parse("2026-08-30T10:15:30Z"), ZoneOffset.UTC);
        service = new DeviceServiceImpl(
                deviceRepository,
                operationService,
                accountRepository,
                deviceTypeService,
                deviceSubTypeService,
                operationStatusRepository,
                nextcloudService,
                linkService,
                clock
        );
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void deleteMarksDeviceAsDeletedWithoutPhysicalDeletion() {
        UUID id = UUID.randomUUID();
        Device device = Device.builder()
                .id(id)
                .serialNumber("SN-001")
                .isDeleted(false)
                .build();
        when(deviceRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(device));

        service.delete(id);

        assertTrue(device.getIsDeleted());
        assertEquals(device.getDeletedAt(), LocalDateTime.now(clock));
        verify(deviceRepository).save(device);
        verify(deviceRepository, never()).delete(device);
    }

    @Test
    public void deletedSerialNumberCanBeUsedAgain() {
        String serialNumber = "SN-001";
        when(deviceRepository.existsBySerialNumberAndIsDeletedFalse(serialNumber)).thenReturn(false);

        assertFalse(service.existsSerialNumber(serialNumber));
    }
}
