package ru.polymetal.labManufacture.data.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "location_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationType {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "type_name", unique = true, nullable = false, length = 50)
    private String typeName;

    @Column(name = "type_description")
    private String typeDescription;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "locationType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DeviceStorageLocation> storageLocations = new HashSet<>();

}
