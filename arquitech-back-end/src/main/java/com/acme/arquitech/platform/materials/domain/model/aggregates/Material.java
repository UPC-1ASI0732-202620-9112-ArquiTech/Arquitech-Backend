package com.acme.arquitech.platform.materials.domain.model.aggregates;

import com.acme.arquitech.platform.materials.domain.model.events.MaterialUsedEvent;
import com.acme.arquitech.platform.materials.domain.model.valueobjects.MaterialStatus;
import com.acme.arquitech.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.DomainEvents;

import java.math.BigDecimal;
import java.util.Collection;

@Getter
@Setter
@Entity
@Table(name = "materials")
public class Material extends AuditableAbstractAggregateRoot<Material> {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Material name is required")
    private String name;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", message = "Unit price must be non-negative")
    private BigDecimal unitPrice;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotBlank(message = "Provider is required")
    private String provider;

    @NotBlank(message = "Provider RUC is required")
    private String providerRuc;

    @NotBlank(message = "Date is required")
    private String date; // Consider LocalDate for better date handling

    @NotBlank(message = "Receipt number is required")
    private String receiptNumber;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    private MaterialStatus status;

    @NotNull(message = "Quantity exit is required")
    @Min(value = 0, message = "Quantity exit must be non-negative")
    private Integer quantityExit;

    @NotBlank(message = "Entry type is required")
    private String entryType;

    private String exitType;

    private String exitDate;

    public Material() {
    }

    public Material(Long projectId, String name, Integer quantity, BigDecimal unitPrice, String unit,
                    String provider, String providerRuc, String date, String receiptNumber,
                    String paymentMethod, MaterialStatus status, Integer quantityExit,
                    String entryType, String exitType, String exitDate) {
        this.projectId = projectId;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.provider = provider;
        this.providerRuc = providerRuc;
        this.date = date;
        this.receiptNumber = receiptNumber;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.quantityExit = quantityExit;
        this.entryType = entryType;
        this.exitType = exitType;
        this.exitDate = exitDate;
    }

    public void useMaterial(Integer usedQuantity, String exitDate) {
        if (usedQuantity > this.quantity - this.quantityExit) {
            throw new IllegalArgumentException("Insufficient stock available");
        }
        this.quantityExit += usedQuantity;
        this.exitType = "Exit";
        this.exitDate = exitDate;
        this.registerEvent(new MaterialUsedEvent(this.id, usedQuantity, exitDate));
    }


    @DomainEvents
    public Collection<Object> domainEvents() {
        return super.domainEvents();
    }
}