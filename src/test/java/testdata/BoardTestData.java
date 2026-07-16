package testdata;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.DeviceType;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.Role;
import ru.polymetal.labManufacture.data.repository.OperationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class BoardTestData {
//    @Column(name = "sn", nullable = false, length = 100)
//    private String serialNumber;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_devices_type"))
//    private DeviceType type;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "subtype_id", nullable = false, foreignKey = @ForeignKey(name = "fk_devices_subtype"))
//    private DeviceSubType subtype;
//
//    @Column(name = "is_deleted", nullable = false)
//    @Builder.Default
//    private Boolean isDeleted = false;
//
//    @Column(name = "deleted_at")
//    private LocalDateTime deletedAt;
//
//    @Column(name = "url_pdf")
//    private String urlPDF;
//
//    @Column(name = "url_txt")
//    private String urlTXT;
//
//    @Column(name = "url_pdf_read")
//    private String urlPDFRead;
//
//    @Column(name = "url_txt_read")
//    private String urlTXTRead;
//
//    @CreationTimestamp
//    @Column(name = "created_time", updatable = false)
//    private LocalDateTime createdTime;
//
//    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @OrderBy("createdTime DESC")
//    private List<Operation> operations;



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
