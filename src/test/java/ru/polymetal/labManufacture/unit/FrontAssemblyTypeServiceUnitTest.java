package ru.polymetal.labManufacture.unit;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.FrontAssemblyType;
import ru.polymetal.labManufacture.data.repository.DeviceSubTypeRepository;
import ru.polymetal.labManufacture.data.repository.FrontAssemblyTypeRepository;
import ru.polymetal.labManufacture.dto.FrontAssemblyTypeDto;
import ru.polymetal.labManufacture.service.FrontAssemblyTypeService;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class FrontAssemblyTypeServiceUnitTest {

    @Mock
    private FrontAssemblyTypeRepository frontAssemblyTypeRepository;
    @Mock
    private DeviceSubTypeRepository deviceSubTypeRepository;

    private AutoCloseable mocks;
    private FrontAssemblyTypeService service;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new FrontAssemblyTypeService(frontAssemblyTypeRepository, deviceSubTypeRepository);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void savePersistsBoardSubtypeLinksAndPdfDocuments() throws Exception {
        UUID motherboardId = UUID.randomUUID();
        UUID keyboardBoardId = UUID.randomUUID();
        DeviceSubType motherboard = DeviceSubType.builder().id(motherboardId).name("Материнская").build();
        DeviceSubType keyboardBoard = DeviceSubType.builder().id(keyboardBoardId).name("Клавиатура").build();
        FrontAssemblyTypeDto form = new FrontAssemblyTypeDto(
                "  Тип А  ", "  Описание типа  ", motherboardId, keyboardBoardId);
        byte[] instructionContent = "%PDF-1.4 instruction".getBytes();
        byte[] diagramContent = "%PDF-1.4 diagram".getBytes();
        MockMultipartFile instruction = new MockMultipartFile(
                "assemblyInstruction", "instruction.pdf", "application/pdf", instructionContent);
        MockMultipartFile diagram = new MockMultipartFile(
                "assemblyDiagram", "diagram.pdf", "application/pdf", diagramContent);

        when(frontAssemblyTypeRepository.findByNameIgnoreCaseAndIsDeletedFalse("Тип А"))
                .thenReturn(Optional.empty());
        when(deviceSubTypeRepository.findById(motherboardId)).thenReturn(Optional.of(motherboard));
        when(deviceSubTypeRepository.findById(keyboardBoardId)).thenReturn(Optional.of(keyboardBoard));

        service.save(form, instruction, diagram);

        ArgumentCaptor<FrontAssemblyType> captor = ArgumentCaptor.forClass(FrontAssemblyType.class);
        verify(frontAssemblyTypeRepository).save(captor.capture());
        FrontAssemblyType saved = captor.getValue();
        assertEquals(saved.getName(), "Тип А");
        assertEquals(saved.getDescription(), "Описание типа");
        assertEquals(saved.getMotherboardSubtype(), motherboard);
        assertEquals(saved.getKeyboardBoardSubtype(), keyboardBoard);
        assertEquals(saved.getAssemblyInstruction(), instructionContent);
        assertEquals(saved.getAssemblyInstructionFileName(), "instruction.pdf");
        assertEquals(saved.getAssemblyInstructionMimeType(), "application/pdf");
        assertEquals(saved.getAssemblyDiagram(), diagramContent);
        assertEquals(saved.getAssemblyDiagramFileName(), "diagram.pdf");
        assertEquals(saved.getAssemblyDiagramMimeType(), "application/pdf");
    }

    @Test
    public void saveAllowsBothDocumentsToBeOmitted() throws Exception {
        UUID motherboardId = UUID.randomUUID();
        UUID keyboardBoardId = UUID.randomUUID();
        DeviceSubType motherboard = DeviceSubType.builder().id(motherboardId).build();
        DeviceSubType keyboardBoard = DeviceSubType.builder().id(keyboardBoardId).build();
        FrontAssemblyTypeDto form = new FrontAssemblyTypeDto(
                "Тип без файлов", "Описание", motherboardId, keyboardBoardId);

        when(frontAssemblyTypeRepository.findByNameIgnoreCaseAndIsDeletedFalse("Тип без файлов"))
                .thenReturn(Optional.empty());
        when(deviceSubTypeRepository.findById(motherboardId)).thenReturn(Optional.of(motherboard));
        when(deviceSubTypeRepository.findById(keyboardBoardId)).thenReturn(Optional.of(keyboardBoard));

        service.save(form, null, null);

        ArgumentCaptor<FrontAssemblyType> captor = ArgumentCaptor.forClass(FrontAssemblyType.class);
        verify(frontAssemblyTypeRepository).save(captor.capture());
        assertNull(captor.getValue().getAssemblyInstruction());
        assertNull(captor.getValue().getAssemblyDiagram());
    }

    @Test
    public void saveRejectsDuplicateActiveName() {
        UUID motherboardId = UUID.randomUUID();
        UUID keyboardBoardId = UUID.randomUUID();
        FrontAssemblyTypeDto form = new FrontAssemblyTypeDto(
                "Повтор", "Описание", motherboardId, keyboardBoardId);

        when(frontAssemblyTypeRepository.findByNameIgnoreCaseAndIsDeletedFalse("Повтор"))
                .thenReturn(Optional.of(new FrontAssemblyType()));

        IllegalArgumentException error = expectThrows(
                IllegalArgumentException.class, () -> service.save(form, null, null));

        assertEquals(error.getMessage(), "Тип передней части с таким именем уже существует");
        verify(frontAssemblyTypeRepository, never()).save(any());
        verify(deviceSubTypeRepository, never()).findById(any());
    }

    @Test
    public void saveRejectsNonPdfDocument() {
        UUID motherboardId = UUID.randomUUID();
        UUID keyboardBoardId = UUID.randomUUID();
        FrontAssemblyTypeDto form = new FrontAssemblyTypeDto(
                "Тип А", "Описание", motherboardId, keyboardBoardId);
        MockMultipartFile invalidInstruction = new MockMultipartFile(
                "assemblyInstruction", "instruction.txt", "text/plain", new byte[]{1});

        when(frontAssemblyTypeRepository.findByNameIgnoreCaseAndIsDeletedFalse("Тип А"))
                .thenReturn(Optional.empty());

        IllegalArgumentException error = expectThrows(
                IllegalArgumentException.class,
                () -> service.save(form, invalidInstruction, null));

        assertEquals(error.getMessage(), "Инструкция сборки: допустим только PDF-файл");
        verify(frontAssemblyTypeRepository, never()).save(any());
        verify(deviceSubTypeRepository, never()).findById(any());
    }

    @Test
    public void saveRejectsPdfExtensionWithInvalidContent() {
        UUID motherboardId = UUID.randomUUID();
        UUID keyboardBoardId = UUID.randomUUID();
        FrontAssemblyTypeDto form = new FrontAssemblyTypeDto(
                "Тип А", "Описание", motherboardId, keyboardBoardId);
        MockMultipartFile invalidInstruction = new MockMultipartFile(
                "assemblyInstruction", "instruction.pdf", "application/pdf", "not a pdf".getBytes());

        when(frontAssemblyTypeRepository.findByNameIgnoreCaseAndIsDeletedFalse("Тип А"))
                .thenReturn(Optional.empty());

        IllegalArgumentException error = expectThrows(
                IllegalArgumentException.class,
                () -> service.save(form, invalidInstruction, null));

        assertEquals(error.getMessage(), "Инструкция сборки: содержимое файла не является PDF");
        verify(frontAssemblyTypeRepository, never()).save(any());
        verify(deviceSubTypeRepository, never()).findById(any());
    }

    @Test
    public void saveRejectsMissingBoardSubtype() {
        UUID motherboardId = UUID.randomUUID();
        UUID keyboardBoardId = UUID.randomUUID();
        FrontAssemblyTypeDto form = new FrontAssemblyTypeDto(
                "Тип А", "Описание", motherboardId, keyboardBoardId);

        when(frontAssemblyTypeRepository.findByNameIgnoreCaseAndIsDeletedFalse("Тип А"))
                .thenReturn(Optional.empty());
        when(deviceSubTypeRepository.findById(motherboardId)).thenReturn(Optional.empty());

        IllegalArgumentException error = expectThrows(
                IllegalArgumentException.class, () -> service.save(form, null, null));

        assertEquals(error.getMessage(), "Тип материнской платы не найден");
        verify(frontAssemblyTypeRepository, never()).save(any());
    }

    @Test
    public void editUpdatesFieldsAndKeepsExistingDocumentsWhenNoFilesSelected() throws Exception {
        UUID typeId = UUID.randomUUID();
        UUID motherboardId = UUID.randomUUID();
        UUID keyboardBoardId = UUID.randomUUID();
        DeviceSubType motherboard = DeviceSubType.builder().id(motherboardId).build();
        DeviceSubType keyboardBoard = DeviceSubType.builder().id(keyboardBoardId).build();
        FrontAssemblyType type = new FrontAssemblyType();
        type.setId(typeId);
        type.setName("Старое имя");
        type.setAssemblyInstruction(new byte[]{1});
        type.setAssemblyDiagram(new byte[]{2});
        FrontAssemblyTypeDto form = new FrontAssemblyTypeDto(
                " Новое имя ", " Новое описание ", motherboardId, keyboardBoardId);

        when(frontAssemblyTypeRepository.findByIdAndIsDeletedFalse(typeId)).thenReturn(Optional.of(type));
        when(frontAssemblyTypeRepository.findByNameIgnoreCaseAndIsDeletedFalse("Новое имя"))
                .thenReturn(Optional.empty());
        when(deviceSubTypeRepository.findById(motherboardId)).thenReturn(Optional.of(motherboard));
        when(deviceSubTypeRepository.findById(keyboardBoardId)).thenReturn(Optional.of(keyboardBoard));

        service.edit(typeId, form, null, null);

        assertEquals(type.getName(), "Новое имя");
        assertEquals(type.getDescription(), "Новое описание");
        assertSame(type.getMotherboardSubtype(), motherboard);
        assertSame(type.getKeyboardBoardSubtype(), keyboardBoard);
        assertEquals(type.getAssemblyInstruction(), new byte[]{1});
        assertEquals(type.getAssemblyDiagram(), new byte[]{2});
        verify(frontAssemblyTypeRepository).save(type);
    }

    @Test
    public void editRejectsAnotherActiveTypeWithSameName() {
        UUID typeId = UUID.randomUUID();
        FrontAssemblyType type = new FrontAssemblyType();
        type.setId(typeId);
        FrontAssemblyType duplicate = new FrontAssemblyType();
        duplicate.setId(UUID.randomUUID());
        FrontAssemblyTypeDto form = new FrontAssemblyTypeDto(
                "Повтор", "Описание", UUID.randomUUID(), UUID.randomUUID());

        when(frontAssemblyTypeRepository.findByIdAndIsDeletedFalse(typeId)).thenReturn(Optional.of(type));
        when(frontAssemblyTypeRepository.findByNameIgnoreCaseAndIsDeletedFalse("Повтор"))
                .thenReturn(Optional.of(duplicate));

        IllegalArgumentException error = expectThrows(
                IllegalArgumentException.class, () -> service.edit(typeId, form, null, null));

        assertEquals(error.getMessage(), "Тип передней части с таким именем уже существует");
        verify(frontAssemblyTypeRepository, never()).save(any());
    }

    @Test
    public void deleteMarksTypeAsDeleted() {
        UUID typeId = UUID.randomUUID();
        FrontAssemblyType type = new FrontAssemblyType();
        type.setId(typeId);
        when(frontAssemblyTypeRepository.findByIdAndIsDeletedFalse(typeId)).thenReturn(Optional.of(type));

        service.delete(typeId);

        assertTrue(type.getIsDeleted());
        verify(frontAssemblyTypeRepository).save(type);
    }
}
