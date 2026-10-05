package ru.polymetal.labManufacture.dto;

import java.util.UUID;

public record FrontAssemblyCreateDto(String caseId, UUID frontAssemblyType,
                                     String motherboardSerialNumber, String keyboardBoardSerialNumber) {
}
