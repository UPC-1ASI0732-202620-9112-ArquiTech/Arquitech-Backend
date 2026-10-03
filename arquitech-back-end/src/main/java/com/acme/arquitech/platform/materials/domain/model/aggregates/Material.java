package com.acme.arquitech.platform.materials.domain.model.aggregates;
import com.acme.arquitech.platform.materials.domain.exception.*;
import com.acme.arquitech.platform.materials.domain.model.valueobjects.MaterialStatus;
import com.acme.arquitech.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "materials")
public class Material extends AuditableAbstractAggregateRoot<Material> {
    private Long projectId;
    private String name;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal stock;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal minimumStock;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;
    private String unit;
    private String provider;
    private String providerRuc;
    // Keep the legacy VARCHAR column; expose LocalDate in REST.
    private String date;
    // Preserved for deployed schemas. New inventory operations do not use these fields.
    @Deprecated private String receiptNumber = "N/A";
    @Deprecated private String paymentMethod = "N/A";
    @Deprecated @Enumerated(EnumType.STRING) private MaterialStatus status = MaterialStatus.RECEIVED;
    @Deprecated private Integer quantityExit = 0;
    @Deprecated private String entryType = "ENTRY";
    @Deprecated private String exitType;
    @Deprecated private String exitDate;

    public Material(Long projectId, String name, String unit, BigDecimal quantity, BigDecimal minimumStock,
                    BigDecimal unitPrice, String provider, String providerRuc, LocalDate date) {
        if (quantity.compareTo(BigDecimal.ZERO) < 0) throw new InvalidMaterialDataException("Quantity must be non-negative");
        this.projectId = projectId;
        this.quantity = quantity;
        this.stock = quantity;
        this.date = date.toString();
        updateDetails(name, unit, minimumStock, unitPrice, provider, providerRuc);
    }

    public void updateDetails(String name, String unit, BigDecimal minimumStock, BigDecimal unitPrice,
                              String provider, String providerRuc) {
        if (minimumStock.compareTo(BigDecimal.ZERO) < 0 || unitPrice.signum() < 0)
            throw new InvalidMaterialDataException("Negative price or minimum stock");
        this.name = name;
        this.unit = unit;
        this.minimumStock = minimumStock;
        this.unitPrice = unitPrice;
        this.provider = provider;
        this.providerRuc = providerRuc;
    }

    // Read compatibility for rows created before stock existed; the first write persists the baseline.
    public BigDecimal getStock() {
        if (stock != null) return stock;
        var legacyExit = BigDecimal.valueOf(quantityExit == null ? 0 : quantityExit);
        return quantity.subtract(legacyExit).max(BigDecimal.ZERO);
    }
    public BigDecimal getMinimumStock() { return minimumStock == null ? BigDecimal.ZERO : minimumStock; }

    public void enter(BigDecimal amount, LocalDate occurredOn) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new InvalidMaterialDataException("Quantity must be positive");
        quantity = quantity.add(amount);
        stock = getStock().add(amount);
        updateStockDate(occurredOn);
    }

    public void use(BigDecimal amount, LocalDate occurredOn) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new InvalidMaterialDataException("Quantity must be positive");
        if (amount.compareTo(getStock()) > 0) throw new InsufficientStockException(id);
        stock = getStock().subtract(amount);
        updateStockDate(occurredOn);
    }

    private void updateStockDate(LocalDate occurredOn) {
        LocalDate previous = date == null ? null : LocalDate.parse(date);
        if (previous == null || occurredOn.isAfter(previous)) date = occurredOn.toString();
    }
}
