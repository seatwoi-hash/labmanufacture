package ru.polymetal.labManufacture.service.operation.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.service.DeviceStatusService;
import ru.polymetal.labManufacture.service.operation.OperationQueryService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationQueryServiceImpl implements OperationQueryService {

    private final OperationService operationService;
    private final DeviceStatusService deviceStatusService;

    @Override
    public List<Operation> findOperationsByStatusNames(Collection<String> statusNames) {

        List<Operation> operations = new ArrayList<>();

        for (String statusName : statusNames) {
            operations.addAll(
                    operationService.findByStatusIdAndIsDelete(
                            deviceStatusService.findByName(statusName).getId()
                    )
            );
        }

        return operations;
    }
}
