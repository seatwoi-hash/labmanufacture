package ru.polymetal.labManufacture.unit;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import ru.polymetal.labManufacture.data.models.OperationStatusRoute;
import ru.polymetal.labManufacture.data.repository.OperationRepository;
import ru.polymetal.labManufacture.data.repository.OperationStatusRouteRepository;
import ru.polymetal.labManufacture.service.operation.OperationService;
import ru.polymetal.labManufacture.service.operation.SubtypeOperationRoutePolicy;
import ru.polymetal.labManufacture.service.operation.Impl.OperationRollbackServiceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * Проверяет универсальный возврат для каждого маршрута из Liquibase-миграций.
 *
 * @author Tatarinov Anton
 */
public class OperationRollbackAllRoutesUnitTest {

    private static final String[] ROUTE_RESOURCES = {
            "db/changelog/v1.6/operation_status_routes.sql",
            "db/changelog/v1.7/complete_operation_status_routes.sql"
    };
    private static final int MIN_EXPECTED_ROUTE_COUNT = 67;
    private static final Pattern ROUTE_PATTERN = Pattern.compile(
            "\\((?:NULL|'([^']*)'),\\s*'([^']+)',\\s*'([^']+)',\\s*'([^']+)'\\)");

    @Mock
    private OperationRepository operationRepository;
    @Mock
    private OperationStatusRouteRepository routeRepository;
    @Mock
    private OperationService operationService;
    @Mock
    private SubtypeOperationRoutePolicy subtypeOperationRoutePolicy;
    @Mock
    private OperationStatusRoute operationStatusRoute;

    private AutoCloseable mocks;
    private OperationRollbackServiceImpl rollbackService;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        rollbackService = new OperationRollbackServiceImpl(
                operationRepository, routeRepository, operationService, subtypeOperationRoutePolicy);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @DataProvider(name = "allOperationStatusRoutes")
    public Object[][] allOperationStatusRoutes() throws IOException {
        List<Object[]> routes = new ArrayList<>();
        for (String resource : ROUTE_RESOURCES) {
            String sql = readResource(resource);
            Matcher matcher = ROUTE_PATTERN.matcher(sql);
            while (matcher.find()) {
                routes.add(new Object[]{
                        matcher.group(1),
                        matcher.group(2),
                        matcher.group(3),
                        matcher.group(4)
                });
            }
        }

        assertFalse(routes.isEmpty(), "Маршруты операций не найдены в миграциях");
        org.testng.Assert.assertTrue(routes.size() >= MIN_EXPECTED_ROUTE_COUNT,
                "Из миграций прочитаны не все маршруты операций");
        return routes.toArray(Object[][]::new);
    }

    @Test(dataProvider = "allOperationStatusRoutes")
    public void rollbackToSupportsEveryConfiguredOperationRoute(
            String previousStatusName,
            String targetStatusName,
            String nextStatusName,
            String operationName) {
        UUID operationId = UUID.randomUUID();
        UUID targetOperationId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        Account account = Account.builder().username("admin").build();
        DeviceSubType subtype = DeviceSubType.builder()
                .isSideTwo(true)
                .isInstallationOne(true)
                .isTestTwo(true)
                .build();
        Device device = Device.builder().id(deviceId).subtype(subtype).build();
        OperationStatus currentStatus = status("ready");
        OperationStatus targetStatus = status(targetStatusName);
        Operation currentOperation = operation(
                device, currentStatus, false, LocalDateTime.of(2026, 8, 31, 12, 0));
        Operation targetOperation = operation(
                device, targetStatus, true, LocalDateTime.of(2026, 8, 30, 12, 0));
        String comment = "Проверка маршрута " + targetStatusName;
        String rollbackDescription = "Возвращён - " + comment;

        when(operationService.findById(operationId)).thenReturn(Optional.of(currentOperation));
        when(operationService.findById(targetOperationId)).thenReturn(Optional.of(targetOperation));
        when(routeRepository.findFirstByCurrentStatus_NameOrderById(targetStatusName))
                .thenReturn(Optional.of(operationStatusRoute));
        when(operationStatusRoute.getNextOperationName()).thenReturn(operationName);
        when(subtypeOperationRoutePolicy.isAllowed(same(subtype), anyString())).thenReturn(true);
        when(operationService.completeRollbackOperation(
                operationId, account, targetStatusName, rollbackDescription)).thenReturn(resultId);

        UUID actual = rollbackService.rollbackTo(
                operationId, targetOperationId, account, comment);

        assertEquals(actual, resultId,
                "Не выполнен маршрут %s -> %s -> %s"
                        .formatted(previousStatusName, targetStatusName, nextStatusName));
        verify(subtypeOperationRoutePolicy).isAllowed(subtype, operationName);
        verify(operationService).completeRollbackOperation(
                operationId, account, targetStatusName, rollbackDescription);
    }

    private String readResource(String resource) throws IOException {
        try (InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IOException("Не найден тестовый ресурс: " + resource);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private OperationStatus status(String name) {
        return OperationStatus.builder().id(UUID.randomUUID()).name(name).build();
    }

    private Operation operation(Device device, OperationStatus status,
                                boolean deleted, LocalDateTime createdTime) {
        return Operation.builder()
                .device(device)
                .status(status)
                .isDeleted(deleted)
                .createdTime(createdTime)
                .build();
    }
}
