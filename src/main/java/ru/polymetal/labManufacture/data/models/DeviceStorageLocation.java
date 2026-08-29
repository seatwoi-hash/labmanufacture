package ru.polymetal.labManufacture.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель данных DeviceStorageLocation.
 *
 * @author Tatarinov Anton
 */
@Entity
@Table(name = "device_storage_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceStorageLocation {

    @Id
    @GeneratedValue
    @Column(name = "location_id", updatable = false, nullable = false)
    private UUID locationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Operation device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_types_id", nullable = false)
    private LocationType locationType;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = true;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

}
