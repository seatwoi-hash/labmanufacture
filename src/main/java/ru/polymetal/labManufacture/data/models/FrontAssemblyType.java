package ru.polymetal.labManufacture.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "front_assembly_types")
@Getter
@Setter
@NoArgsConstructor
public class FrontAssemblyType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "motherboard_subtype_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_front_assembly_types_motherboard_subtype"))
    private DeviceSubType motherboardSubtype;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "keyboard_board_subtype_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_front_assembly_types_keyboard_subtype"))
    private DeviceSubType keyboardBoardSubtype;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "assembly_instruction")
    private byte[] assemblyInstruction;

    @Column(name = "assembly_instruction_file_name", length = 512)
    private String assemblyInstructionFileName;

    @Column(name = "assembly_instruction_mime_type", length = 127)
    private String assemblyInstructionMimeType;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "assembly_diagram")
    private byte[] assemblyDiagram;

    @Column(name = "assembly_diagram_file_name", length = 512)
    private String assemblyDiagramFileName;

    @Column(name = "assembly_diagram_mime_type", length = 127)
    private String assemblyDiagramMimeType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
