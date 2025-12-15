package ru.polymetal.labManufacture.data.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "device_relations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRelation {

    @EmbeddedId
    private DeviceRelationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("assemblyId")
    @JoinColumn(name = "device_id_assembly")
    private Device assembly;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("partId")
    @JoinColumn(name = "device_id_part")
    private Device part;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

}
