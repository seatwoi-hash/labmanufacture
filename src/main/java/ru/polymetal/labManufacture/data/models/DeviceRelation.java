package ru.polymetal.labManufacture.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Модель данных DeviceRelation.
 *
 * @author Tatarinov Anton
 */
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
    private Operation assembly;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("partId")
    @JoinColumn(name = "device_id_part")
    private Operation part;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

}
