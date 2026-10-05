package ru.polymetal.labManufacture.data.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "front_assemblies")
@Getter
@Setter
@NoArgsConstructor
public class FrontAssembly {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false, unique = true, length = 100)
    private String caseId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "front_assembly_type_id", nullable = false)
    private FrontAssemblyType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "motherboard_device_id", nullable = false)
    private Device motherboard;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "keyboard_board_device_id", nullable = false)
    private Device keyboardBoard;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "frontAssembly", fetch = FetchType.LAZY)
    @OrderBy("createdTime DESC")
    private List<FrontAssemblyOperation> operations = new ArrayList<>();

    public String getMotherboardSerialNumber() { return motherboard.getSerialNumber(); }
    public String getKeyboardBoardSerialNumber() { return keyboardBoard.getSerialNumber(); }
    public String getFileUrl() { return null; }
}
