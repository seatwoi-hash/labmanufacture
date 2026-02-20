package ru.polymetal.labManufacture.service;


import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.polymetal.labManufacture.data.models.FileData;
import ru.polymetal.labManufacture.dto.FileResponseDto;
import ru.polymetal.labManufacture.dto.FileUploadRequestDto;
import java.io.IOException;
import java.util.UUID;

@Service
public interface FileService {

    FileResponseDto uploadFile(MultipartFile file, FileUploadRequestDto request) throws IOException;

    FileData buildFileData(MultipartFile file, FileUploadRequestDto request,
                           String newFileName, byte[] byteFile) throws IOException;
    FileData getFile(UUID id);

    void delete(UUID id);

}
