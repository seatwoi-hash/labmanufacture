package ru.polymetal.labManufacture.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Объект передачи данных FileResponseDto.
 *
 * @author Tatarinov Anton
 */
@Data
@Builder
public class FileResponseDto {

    private UUID id;
    private String originalName;
    private String newName;
    private String mimeType;
    private Long fileSize;
    private UUID operationsId;
    private UUID accountId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
