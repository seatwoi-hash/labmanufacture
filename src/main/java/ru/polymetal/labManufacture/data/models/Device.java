package ru.polymetal.labManufacture.data.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "devices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "sn", nullable = false, length = 100)
    private String serialNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_devices_type"))
    private DeviceType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtype_id", nullable = false, foreignKey = @ForeignKey(name = "fk_devices_subtype"))
    private DeviceSubType subtype;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "url_pdf")
    private String urlPDF;

    @Column(name = "url_txt")
    private String urlTXT;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

     @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     @OrderBy("createdTime DESC")
     private List<Operation> operations;



    public Operation getDisplayStatus() {
        if (operations == null || operations.isEmpty()) {
            return null;
        }

        // Получаем последний статус
        Operation lastOperation = operations.get(0);
        String lastStatus = lastOperation.getStatus().getName();

        // Проверяем, является ли последний статус Technical/Technical2/Technical3
        if (isTechnicalStatus(lastStatus)) {
            // Если есть хотя бы 2 операции, показываем предпоследнюю
            if (operations.size() >= 2) {
                Operation secondLastOperation = operations.get(1);
                return secondLastOperation;
            } else {
                // Если только одна операция, показываем её
                return lastOperation;
            }
        }

        return lastOperation;
    }

    private boolean isTechnicalStatus(String status) {
        return "Technical".equals(status)
                || "Technical2".equals(status)
                || "Technical3".equals(status);
    }
}
