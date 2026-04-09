package ru.polymetal.labManufacture.data.models;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRelationId implements Serializable {

    @Column(name = "device_id_assembly")
    private UUID assemblyId;

    @Column(name = "device_id_part")
    private UUID partId;

}
