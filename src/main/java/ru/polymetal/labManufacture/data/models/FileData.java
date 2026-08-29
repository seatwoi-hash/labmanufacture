package ru.polymetal.labManufacture.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель данных FileData.
 *
 * @author Tatarinov Anton
 */
@Entity
@Table(name = "file_data")
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "original_name", length = 512)
    private String originalName;

    @Column(name = "new_name", length = 512)
    private String newName;

    @Column(name = "mime_type", length = 127)
    private String mimeType;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "data")
    private byte[] data;

    @OneToOne
    @JoinColumn(name = "operations_id", referencedColumnName = "id", nullable = false, unique = true)
    private Operation operation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
