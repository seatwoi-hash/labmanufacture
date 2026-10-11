package ru.polymetal.labManufacture.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.polymetal.labManufacture.constant.FrontAssemblyStatusCode;
import ru.polymetal.labManufacture.data.models.*;
import ru.polymetal.labManufacture.data.repository.*;
import ru.polymetal.labManufacture.dto.FrontAssemblyCreateDto;

import java.time.LocalDateTime;
import java.util.*;

import static ru.polymetal.labManufacture.constant.FrontAssemblyStatusCode.*;

@Service
public class FrontAssemblyFlowService {
    private final FrontAssemblyRepository assemblyRepository;
    private final FrontAssemblyTypeRepository typeRepository;
    private final FrontAssemblyStatusRepository statusRepository;
    private final FrontAssemblyOperationRepository operationRepository;
    private final DeviceRepository deviceRepository;
    private final OperationRepository boardOperationRepository;

    public FrontAssemblyFlowService(FrontAssemblyRepository assemblyRepository,
                                    FrontAssemblyTypeRepository typeRepository,
                                    FrontAssemblyStatusRepository statusRepository,
                                    FrontAssemblyOperationRepository operationRepository,
                                    DeviceRepository deviceRepository,
                                    OperationRepository boardOperationRepository) {
        this.assemblyRepository = assemblyRepository;
        this.typeRepository = typeRepository;
        this.statusRepository = statusRepository;
        this.operationRepository = operationRepository;
        this.deviceRepository = deviceRepository;
        this.boardOperationRepository = boardOperationRepository;
    }

    @Transactional
    public FrontAssembly create(FrontAssemblyCreateDto form, Account account) {
        String caseId = requireText(form.caseId(), "Укажите ID корпуса");
        if (assemblyRepository.existsByCaseIdIgnoreCaseAndIsDeletedFalse(caseId)) {
            throw new IllegalArgumentException("Сборка с таким ID корпуса уже существует");
        }
        FrontAssemblyType type = typeRepository.findByIdAndIsDeletedFalse(
                Objects.requireNonNull(form.frontAssemblyType(), "Выберите тип передней части моноблока"))
                .orElseThrow(() -> new IllegalArgumentException("Тип передней части моноблока не найден"));
        Device motherboard = readyBoard(form.motherboardSerialNumber(), "материнскую плату");
        Device keyboard = readyBoard(form.keyboardBoardSerialNumber(), "плату клавиатуры");
        if (motherboard.getId().equals(keyboard.getId())) {
            throw new IllegalArgumentException("Для сборки нужно выбрать две разные платы");
        }
        if (!motherboard.getSubtype().getId().equals(type.getMotherboardSubtype().getId())) {
            throw new IllegalArgumentException("Тип материнской платы не соответствует выбранной сборке");
        }
        if (!keyboard.getSubtype().getId().equals(type.getKeyboardBoardSubtype().getId())) {
            throw new IllegalArgumentException("Тип платы клавиатуры не соответствует выбранной сборке");
        }

        FrontAssembly assembly = new FrontAssembly();
        assembly.setCaseId(caseId);
        assembly.setType(type);
        assembly.setMotherboard(motherboard);
        assembly.setKeyboardBoard(keyboard);
        assemblyRepository.save(assembly);
        saveOperation(assembly, account, QUALITY_CHECK_1, "Сборка создана", null);
        return assembly;
    }

    @Transactional(readOnly = true)
    public List<FrontAssemblyOperation> findByStatuses(Collection<FrontAssemblyStatusCode> statuses) {
        return operationRepository.findActiveByStatusNames(statuses.stream().map(FrontAssemblyStatusCode::getCode).toList());
    }

    @Transactional(readOnly = true)
    public List<FrontAssemblyOperation> findInProgress() {
        return findByStatuses(List.of(
                QUALITY_CHECK_1, REPAIR_1, QUALITY_CHECK_2, TEST_1,
                DIAGNOSTICS, REPAIR_2, QUALITY_CHECK_3, TEST_2));
    }

    @Transactional(readOnly = true)
    public List<FrontAssemblyOperation> findHistory(UUID assemblyId) {
        assemblyRepository.findByIdAndIsDeletedFalse(assemblyId)
                .orElseThrow(() -> new IllegalArgumentException("Сборка не найдена"));
        return operationRepository.findHistoryByAssemblyId(assemblyId);
    }

    @Transactional(readOnly = true)
    public boolean isBoardUsed(UUID deviceId) {
        return assemblyRepository.existsActiveByBoardId(deviceId);
    }

    @Transactional(readOnly = true)
    public Set<UUID> findUsedBoardIds() {
        Set<UUID> ids = new HashSet<>(assemblyRepository.findActiveMotherboardIds());
        ids.addAll(assemblyRepository.findActiveKeyboardBoardIds());
        return ids;
    }

    @Transactional
    public void deleteInProgress(UUID assemblyId) {
        FrontAssemblyOperation activeOperation = operationRepository.findActiveForUpdate(assemblyId)
                .orElseThrow(() -> new IllegalArgumentException("Активная операция сборки не найдена"));
        if (codeOf(activeOperation.getStatus().getName()) == READY) {
            throw new IllegalStateException("Готовую сборку нельзя удалить со страницы сборок в работе");
        }

        LocalDateTime deletedAt = LocalDateTime.now();
        activeOperation.setIsDeleted(true);
        activeOperation.setDeletedAt(deletedAt);
        operationRepository.save(activeOperation);

        FrontAssembly assembly = activeOperation.getFrontAssembly();
        assembly.setIsDeleted(true);
        assembly.setDeletedAt(deletedAt);
        assemblyRepository.save(assembly);
    }

    @Transactional
    public void transition(UUID assemblyId, String action, String description, Account account) {
        FrontAssemblyOperation current = operationRepository.findActiveForUpdate(assemblyId)
                .orElseThrow(() -> new IllegalArgumentException("Активная операция сборки не найдена"));
        FrontAssemblyStatusCode currentCode = codeOf(current.getStatus().getName());
        FrontAssemblyStatusCode next = nextStatus(currentCode, action);
        if (!"passed".equals(action) && (description == null || description.isBlank())) {
            throw new IllegalArgumentException("Комментарий обязателен для выбранного действия");
        }
        current.setIsDeleted(true);
        current.setDeletedAt(LocalDateTime.now());
        operationRepository.save(current);
        if ("failed".equals(action)) {
            FrontAssemblyOperation failedOperation = saveOperation(
                    current.getFrontAssembly(), account, failureStatus(currentCode), description, action);
            failedOperation.setIsDeleted(true);
            failedOperation.setDeletedAt(LocalDateTime.now());
            operationRepository.save(failedOperation);
            saveOperation(current.getFrontAssembly(), account, next, null, null);
            return;
        }
        saveOperation(current.getFrontAssembly(), account, next, description, action);
    }

    private FrontAssemblyStatusCode nextStatus(FrontAssemblyStatusCode current, String action) {
        return switch (current) {
            case QUALITY_CHECK_1 -> actionResult(action, TEST_1, REPAIR_1);
            case REPAIR_1 -> requireAction(action, "complete", QUALITY_CHECK_2);
            case QUALITY_CHECK_2 -> actionResult(action, TEST_1, REPAIR_1);
            case TEST_1 -> actionResult(action, READY, DIAGNOSTICS);
            case DIAGNOSTICS -> switch (action) {
                case "test" -> TEST_2;
                case "repair" -> REPAIR_2;
                default -> throw invalidAction(current, action);
            };
            case REPAIR_2 -> requireAction(action, "complete", QUALITY_CHECK_3);
            case QUALITY_CHECK_3 -> actionResult(action, TEST_2, REPAIR_2);
            case TEST_2 -> actionResult(action, READY, DIAGNOSTICS);
            default -> throw new IllegalStateException("Переход из статуса «" + current.getCode() + "» запрещён");
        };
    }

    private FrontAssemblyStatusCode actionResult(String action, FrontAssemblyStatusCode passed,
                                                  FrontAssemblyStatusCode failed) {
        return switch (action) {
            case "passed" -> passed;
            case "failed" -> failed;
            default -> throw new IllegalArgumentException("Недопустимое действие: " + action);
        };
    }

    private FrontAssemblyStatusCode requireAction(String action, String expected,
                                                   FrontAssemblyStatusCode next) {
        if (!expected.equals(action)) {
            throw new IllegalArgumentException("Недопустимое действие: " + action);
        }
        return next;
    }

    private IllegalArgumentException invalidAction(FrontAssemblyStatusCode status, String action) {
        return new IllegalArgumentException(
                "Действие «" + action + "» недопустимо для статуса «" + status.getCode() + "»");
    }

    private FrontAssemblyStatusCode failureStatus(FrontAssemblyStatusCode source) {
        return switch (source) {
            case QUALITY_CHECK_1 -> FAIL_QUALITY_CHECK_1;
            case QUALITY_CHECK_2 -> FAIL_QUALITY_CHECK_2;
            case QUALITY_CHECK_3 -> FAIL_QUALITY_CHECK_3;
            case TEST_1 -> FAIL_TEST_1;
            case TEST_2 -> FAIL_TEST_2;
            default -> throw new IllegalStateException(
                    "Отрицательная операция для статуса «" + source.getCode() + "» не определена");
        };
    }

    private Device readyBoard(String serialNumber, String fieldName) {
        Device device = deviceRepository.findOneBySerialNumberAndIsDeletedFalse(requireText(serialNumber, "Выберите " + fieldName))
                .orElseThrow(() -> new IllegalArgumentException("Выбранная плата не найдена"));
        Operation active = boardOperationRepository.findActiveByDeviceIdWithLock(device.getId())
                .orElseThrow(() -> new IllegalArgumentException("У платы нет активной операции"));
        if (!ru.polymetal.labManufacture.constant.DeviceStatusCodes.READY.getCode().equals(active.getStatus().getName())) {
            throw new IllegalArgumentException("Для сборки можно использовать только готовые платы");
        }
        if (assemblyRepository.existsActiveByBoardId(device.getId())) {
            throw new IllegalArgumentException("Плата уже используется в другой сборке");
        }
        return device;
    }

    private FrontAssemblyOperation saveOperation(FrontAssembly assembly, Account account,
                                                 FrontAssemblyStatusCode code,
                                                 String description, String transitionAction) {
        FrontAssemblyStatus status = statusRepository.findByName(code.getCode())
                .orElseThrow(() -> new IllegalStateException("Статус сборки не найден: " + code.getCode()));
        FrontAssemblyOperation operation = new FrontAssemblyOperation();
        operation.setFrontAssembly(assembly);
        operation.setAccount(account);
        operation.setStatus(status);
        operation.setDescription(description == null ? null : description.trim());
        operation.setTransitionAction(transitionAction);
        return operationRepository.save(operation);
    }

    private FrontAssemblyStatusCode codeOf(String value) {
        return Arrays.stream(values()).filter(code -> code.getCode().equals(value)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Неизвестный статус сборки: " + value));
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return value.trim();
    }
}
