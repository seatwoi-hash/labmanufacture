package ru.polymetal.labManufacture.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.FrontAssemblyType;
import ru.polymetal.labManufacture.data.repository.DeviceSubTypeRepository;
import ru.polymetal.labManufacture.data.repository.FrontAssemblyTypeRepository;
import ru.polymetal.labManufacture.dto.FrontAssemblyTypeDto;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class FrontAssemblyTypeService {

    private final FrontAssemblyTypeRepository frontAssemblyTypeRepository;
    private final DeviceSubTypeRepository deviceSubTypeRepository;

    public FrontAssemblyTypeService(FrontAssemblyTypeRepository frontAssemblyTypeRepository,
                                    DeviceSubTypeRepository deviceSubTypeRepository) {
        this.frontAssemblyTypeRepository = frontAssemblyTypeRepository;
        this.deviceSubTypeRepository = deviceSubTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<FrontAssemblyType> findAll() {
        return frontAssemblyTypeRepository.findAllByIsDeletedFalseOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public FrontAssemblyType findById(UUID id) {
        return frontAssemblyTypeRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Тип передней части не найден"));
    }

    @Transactional
    public void save(FrontAssemblyTypeDto form,
                     MultipartFile assemblyInstruction,
                     MultipartFile assemblyDiagram) throws IOException {
        String name = form.name().trim();
        if (frontAssemblyTypeRepository.findByNameIgnoreCaseAndIsDeletedFalse(name).isPresent()) {
            throw new IllegalArgumentException("Тип передней части с таким именем уже существует");
        }

        validatePdf(assemblyInstruction, "Инструкция сборки");
        validatePdf(assemblyDiagram, "Сборочная схема");

        DeviceSubType motherboardSubtype = deviceSubTypeRepository.findById(form.motherboardTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Тип материнской платы не найден"));
        DeviceSubType keyboardSubtype = deviceSubTypeRepository.findById(form.keyboardBoardTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Тип платы клавиатуры не найден"));

        FrontAssemblyType type = new FrontAssemblyType();
        type.setName(name);
        type.setDescription(form.description().trim());
        type.setMotherboardSubtype(motherboardSubtype);
        type.setKeyboardBoardSubtype(keyboardSubtype);
        setInstruction(type, assemblyInstruction);
        setDiagram(type, assemblyDiagram);
        frontAssemblyTypeRepository.save(type);
    }

    @Transactional
    public void edit(UUID id,
                     FrontAssemblyTypeDto form,
                     MultipartFile assemblyInstruction,
                     MultipartFile assemblyDiagram) throws IOException {
        FrontAssemblyType type = findById(id);
        String name = form.name().trim();
        frontAssemblyTypeRepository.findByNameIgnoreCaseAndIsDeletedFalse(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Тип передней части с таким именем уже существует");
                });

        validatePdf(assemblyInstruction, "Инструкция сборки");
        validatePdf(assemblyDiagram, "Сборочная схема");

        type.setName(name);
        type.setDescription(form.description().trim());
        type.setMotherboardSubtype(findSubtype(form.motherboardTypeId(), "Тип материнской платы не найден"));
        type.setKeyboardBoardSubtype(findSubtype(form.keyboardBoardTypeId(), "Тип платы клавиатуры не найден"));
        setInstruction(type, assemblyInstruction);
        setDiagram(type, assemblyDiagram);
        frontAssemblyTypeRepository.save(type);
    }

    @Transactional
    public void delete(UUID id) {
        FrontAssemblyType type = findById(id);
        type.setIsDeleted(true);
        frontAssemblyTypeRepository.save(type);
    }

    private DeviceSubType findSubtype(UUID id, String errorMessage) {
        return deviceSubTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(errorMessage));
    }

    private void validatePdf(MultipartFile file, String fieldName) {
        if (file == null || file.isEmpty()) {
            return;
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException(fieldName + ": допустим только PDF-файл");
        }
        try {
            byte[] content = file.getBytes();
            if (content.length < 5
                    || content[0] != '%'
                    || content[1] != 'P'
                    || content[2] != 'D'
                    || content[3] != 'F'
                    || content[4] != '-') {
                throw new IllegalArgumentException(fieldName + ": содержимое файла не является PDF");
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException(fieldName + ": не удалось прочитать файл", exception);
        }
    }

    private void setInstruction(FrontAssemblyType type, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            type.setAssemblyInstruction(file.getBytes());
            type.setAssemblyInstructionFileName(file.getOriginalFilename());
            type.setAssemblyInstructionMimeType(file.getContentType());
        }
    }

    private void setDiagram(FrontAssemblyType type, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            type.setAssemblyDiagram(file.getBytes());
            type.setAssemblyDiagramFileName(file.getOriginalFilename());
            type.setAssemblyDiagramMimeType(file.getContentType());
        }
    }
}
