package ru.polymetal.labManufacture.unit;

import org.testng.annotations.Test;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.security.AccountDetails;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class AccountEqualityUnitTest {

    @Test
    public void differentUsernamesProduceDifferentPrincipals() {
        Account firstAccount = Account.builder().username("first-user").build();
        Account secondAccount = Account.builder().username("second-user").build();

        assertNotEquals(new AccountDetails(firstAccount), new AccountDetails(secondAccount));
    }

    @Test
    public void sameUsernameProducesEqualPrincipals() {
        Account firstAccount = Account.builder().username("same-user").build();
        Account secondAccount = Account.builder().username("same-user").build();

        assertEquals(new AccountDetails(firstAccount), new AccountDetails(secondAccount));
    }
}
