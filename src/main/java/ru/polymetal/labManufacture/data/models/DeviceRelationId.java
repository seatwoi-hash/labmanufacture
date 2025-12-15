package ru.polymetal.labManufacture.data.models;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRelationId implements Serializable {

    @Column(name = "device_id_assembly")
    private UUID assemblyId;

    @Column(name = "device_id_part")
    private UUID partId;

}
