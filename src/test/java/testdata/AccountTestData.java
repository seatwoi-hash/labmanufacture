package testdata;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Role;

import java.util.Set;

public final class AccountTestData {

    private AccountTestData() {
    }

    public static Account createAdmin(PasswordEncoder encoder, Role role) {

        return createAccount(
                "adminTestMVC",
                "admin@test.local",
                encoder,
                role
        );
    }

    public static Account createUser(PasswordEncoder encoder, Role role) {

        return createAccount(
                "userTestMVC",
                "user@test.local",
                encoder,
                role
        );
    }

    public static Account createOperator(PasswordEncoder encoder, Role role) {

        return createAccount(
                "operatorTestMVC",
                "operator@test.local",
                encoder,
                role
        );
    }

    public static Account createQuality(PasswordEncoder encoder, Role role) {

        return createAccount(
                "qualityTestMVC",
                "quality@test.local",
                encoder,
                role
        );
    }

    public static Account createTesterA(PasswordEncoder encoder, Role role) {

        return createAccount(
                "testerATestMVC",
                "testerA@test.local",
                encoder,
                role
        );
    }

    public static Account createTesterB(PasswordEncoder encoder, Role role) {

        return createAccount(
                "testerBTestMVC",
                "testerB@test.local",
                encoder,
                role
        );
    }

    public static Account createAssembler(PasswordEncoder encoder, Role role) {

        return createAccount(
                "assemblerTestMVC",
                "assembler@test.local",
                encoder,
                role
        );
    }

    public static Account createRepairman(PasswordEncoder encoder, Role role) {

        return createAccount(
                "repairmanTestMVC",
                "repairman@test.local",
                encoder,
                role
        );
    }

    public static Account createWasher(PasswordEncoder encoder, Role role) {

        return createAccount(
                "washerTestMVC",
                "washer@test.local",
                encoder,
                role
        );
    }

    public static Account createOutput(PasswordEncoder encoder, Role role) {

        return createAccount(
                "outputTestMVC",
                "output@test.local",
                encoder,
                role
        );
    }

    public static Account createVarnisher(PasswordEncoder encoder, Role role) {

        return createAccount(
                "varnisherTestMVC",
                "varnisher@test.local",
                encoder,
                role
        );
    }

    private static Account createAccount(
            String username,
            String email,
            PasswordEncoder encoder,
            Role role
    ) {

        Account account = new Account();
        account.setUsername(username);
        account.setPasswordHash(encoder.encode("testMVC"));
        account.setFirstName("Тест");
        account.setLastName("Тестов");
        account.setEmail(email);
        account.setRoles(Set.of(role));

        return account;
    }
}
