package ru.polymetal.labManufacture.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.polymetal.labManufacture.data.models.DeviceRelation;
import ru.polymetal.labManufacture.data.models.DeviceRelationId;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceRelationRepository extends JpaRepository<DeviceRelation, DeviceRelationId> {

    List<DeviceRelation> findByAssemblyId(UUID assemblyId);

    List<DeviceRelation> findByPartId(UUID partId);

}
