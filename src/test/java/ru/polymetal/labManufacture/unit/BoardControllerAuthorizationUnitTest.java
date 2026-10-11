package ru.polymetal.labManufacture.unit;

import org.springframework.security.access.prepost.PreAuthorize;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.controller.operation.board.DiffController;
import ru.polymetal.labManufacture.controller.operation.board.InstallationController;
import ru.polymetal.labManufacture.controller.operation.board.RepairController;
import ru.polymetal.labManufacture.controller.operation.board.RollbackOtkController;
import ru.polymetal.labManufacture.controller.operation.board.TestController;
import ru.polymetal.labManufacture.controller.operation.board.VarnishController;
import ru.polymetal.labManufacture.controller.operation.board.WashingController;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class BoardControllerAuthorizationUnitTest {

    @DataProvider
    public Object[][] protectedPostOperations() {
        return new Object[][]{
                {DiffController.class, "completeMOne", "hasAnyRole('ADMIN', 'OPERATOR')"},
                {DiffController.class, "completeMTwo", "hasAnyRole('ADMIN', 'OPERATOR')"},
                {InstallationController.class, "completeInstallation", "hasAnyRole('ADMIN', 'OUTPUT')"},
                {InstallationController.class, "completeInstallationTwo", "hasAnyRole('ADMIN', 'OUTPUT')"},
                {RepairController.class, "completeRepairOne", "hasAnyRole('ADMIN', 'REPAIRMAN')"},
                {RepairController.class, "completeRepairTwo", "hasAnyRole('ADMIN', 'REPAIRMAN')"},
                {RepairController.class, "completeRepairThree", "hasAnyRole('ADMIN', 'REPAIRMAN')"},
                {RepairController.class, "completeRepairFour", "hasAnyRole('ADMIN', 'REPAIRMAN')"},
                {RepairController.class, "completeRepairFive", "hasAnyRole('ADMIN', 'REPAIRMAN')"},
                {RepairController.class, "completeRepairSix", "hasAnyRole('ADMIN', 'REPAIRMAN')"},
                {RollbackOtkController.class, "rollback", "hasAnyRole('ADMIN', 'QUALITY')"},
                {TestController.class, "completeTest", "hasAnyRole('ADMIN', 'TESTERB')"},
                {TestController.class, "completeTestTwo", "hasAnyRole('ADMIN', 'TESTERB')"},
                {VarnishController.class, "completeVarnish", "hasAnyRole('ADMIN', 'VARNISHER')"},
                {WashingController.class, "completeWashingOne", "hasAnyRole('ADMIN', 'WASHER')"},
                {WashingController.class, "completeWashingTwo", "hasAnyRole('ADMIN', 'WASHER')"}
        };
    }

    @Test(dataProvider = "protectedPostOperations")
    public void postOperationHasExpectedRoleRestriction(
            Class<?> controllerClass, String methodName, String expectedExpression) {
        Method method = Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow();

        PreAuthorize authorization = method.getAnnotation(PreAuthorize.class);

        assertNotNull(authorization, methodName + " must be protected");
        assertEquals(authorization.value(), expectedExpression);
    }
}
