package ru.polymetal.labManufacture.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Маршрут перехода между производственными статусами.
 *
 * @author Tatarinov Anton
 */
@Entity
@Table(name = "operation_status_routes")
@Getter
@NoArgsConstructor
public class OperationStatusRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_status_id")
    private OperationStatus previousStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "current_status_id", nullable = false)
    private OperationStatus currentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_status_id")
    private OperationStatus nextStatus;

    @Column(name = "next_operation_name", nullable = false, length = 100)
    private String nextOperationName;

    @Column(name = "previous_operation_name", length = 100)
    private String previousOperationName;
}
