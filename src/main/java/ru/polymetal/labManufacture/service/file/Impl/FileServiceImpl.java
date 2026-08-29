package ru.polymetal.labManufacture.service.file.Impl;

import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.FileData;
import ru.polymetal.labManufacture.data.repository.AccountRepository;
import ru.polymetal.labManufacture.data.repository.FileDataRepository;
import ru.polymetal.labManufacture.dto.FileResponseDto;
import ru.polymetal.labManufacture.dto.FileUploadRequestDto;
import ru.polymetal.labManufacture.service.file.FileService;
import ru.polymetal.labManufacture.service.operation.OperationService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private final FileDataRepository fileDataRepository;
    private final AccountRepository accountRepository;
    private final OperationService operationService;


    public FileServiceImpl(FileDataRepository fileDataRepository, AccountRepository accountRepository,
                           OperationService operationService) {
        this.fileDataRepository = fileDataRepository;
        this.accountRepository = accountRepository;
        this.operationService = operationService;
    }

    @Override
    @Transactional
    public FileResponseDto uploadFile(MultipartFile file, FileUploadRequestDto request) throws IOException {

        log.info("Начата загрузка файла: operationId={}, accountId={}, originalName={}, size={}, contentType={}",
                request.getOperation().getId(), request.getAccount().getId(), file.getOriginalFilename(),
                file.getSize(), file.getContentType());

        fileDataRepository.findByOperation(request.getOperation())
                .ifPresent(existingFile -> {
                    log.info("Замена существующего файла: fileId={}, operationId={}",
                            existingFile.getId(), request.getOperation().getId());
                    fileDataRepository.delete(existingFile);
                    fileDataRepository.flush();
                });


        String newFileName = generateUniqueFileName(request);
        byte[] fileBytes = file.getBytes();
        FileData fileData = buildFileData(file, request, newFileName, fileBytes);
        fileDataRepository.insertFileData(fileData);
        log.info("Файл загружен: fileId={}, operationId={}, storedName={}, size={}",
                fileData.getId(), request.getOperation().getId(), newFileName, fileBytes.length);
        //fileDataRepository.save(fileData);
        return mapToResponse(fileData);
    }

    @Transactional(readOnly = true)
    public FileData getFile(UUID id) {
        log.debug("Получение файла: fileId={}", id);
        return fileDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Файл не найден - id: " + id));
    }

    @Override
    public void delete(UUID id) {
        log.info("Начато удаление файла: fileId={}", id);
        fileDataRepository.delete(fileDataRepository.getReferenceById(id));
        log.info("Файл удалён: fileId={}", id);
    }


    private String generateUniqueFileName(FileUploadRequestDto fileUploadRequestDto) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Account account = accountRepository.findById(fileUploadRequestDto.getAccount().getId()).orElseThrow(
                () -> new RuntimeException("Аккаунт не найден")
        );

        String lastName = account.getLastName();
        String operationName = fileUploadRequestDto.getOperation().getStatus().getName();
        String sn = fileUploadRequestDto.getOperation().getDevice().getSerialNumber();
        return timestamp + "_" + sn + "_" + operationName + "_" + lastName;
    }


    @Override
    public FileData buildFileData(MultipartFile file, FileUploadRequestDto request,
                                  String newFileName, byte[] fileBytes) throws IOException {

        FileData fileData = new FileData();
        fileData.setAccount(request.getAccount());
        fileData.setOperation(request.getOperation());
        fileData.setData(file.getBytes());
        fileData.setMimeType(file.getContentType());
        fileData.setNewName(newFileName);
        fileData.setOriginalName(file.getOriginalFilename());

        return fileData;
    }

    private FileResponseDto mapToResponse(FileData fileData) {
        return FileResponseDto.builder()
                .id(fileData.getId())
                .originalName(fileData.getOriginalName())
                .newName(fileData.getNewName())
                .mimeType(fileData.getMimeType())
                .operationsId(fileData.getOperation().getId())
                .accountId(fileData.getAccount().getId())
                .createdAt(fileData.getCreatedAt())
                .updatedAt(fileData.getUpdatedAt())
                .build();
    }

}
