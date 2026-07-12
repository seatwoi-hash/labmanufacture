package testdata;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.Role;
import java.util.Set;

public class BoardTestData {

    private final
    private static Device createDevice(
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
