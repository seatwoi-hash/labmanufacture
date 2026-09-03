package ru.polymetal.labManufacture.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель данных DeviceSubType.
 *
 * @author Tatarinov Anton
 */
@Entity
@Table(name = "device_subtypes")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceSubType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "sn_type")
    private Integer snType;

    @Column(name = "version_type")
    private Integer versionType;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "archive_original_name", length = 512)
    private String archiveOriginalName;

    @Column(name = "archive_mime_type", length = 127)
    private String archiveMimeType;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "is_installation_one")
    private Boolean isInstallationOne = true;

    @Column(name = "is_test_two")
    private Boolean isTestTwo = true;

    @Column(name = "is_side_two")
    private Boolean isSideTwo = true;

    @Column(name = "url_pdf")
    private String urlPDF;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "data")
    private byte[] data;


}
