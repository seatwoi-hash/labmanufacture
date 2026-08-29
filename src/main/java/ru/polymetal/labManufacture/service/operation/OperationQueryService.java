package ru.polymetal.labManufacture.service.operation;

import ru.polymetal.labManufacture.data.models.Operation;
import java.util.Collection;
import java.util.List;

/**
 * Контракт сервиса OperationQueryService.
 *
 * @author Tatarinov Anton
 */
public interface OperationQueryService {

    /**
     * Возвращает список операций для указанных статусов.
     *
     * @param statusNames имена статусов операций
     * @return список операций
     */
    List<Operation> findOperationsByStatusNames(Collection<String> statusNames);

}
