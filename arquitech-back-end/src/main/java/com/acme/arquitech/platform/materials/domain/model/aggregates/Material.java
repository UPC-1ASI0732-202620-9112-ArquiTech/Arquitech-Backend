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
    private Integer quantity;
    private Integer stock;
    private Integer minimumStock;
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

    public Material(Long projectId, String name, String unit, Integer quantity, Integer minimumStock,
                    BigDecimal unitPrice, String provider, String providerRuc, LocalDate date) {
        if (quantity < 0) throw new InvalidMaterialDataException("Quantity must be non-negative");
        this.projectId = projectId;
        this.quantity = quantity;
        this.stock = quantity;
        this.date = date.toString();
        updateDetails(name, unit, minimumStock, unitPrice, provider, providerRuc);
    }

    public void updateDetails(String name, String unit, Integer minimumStock, BigDecimal unitPrice,
                              String provider, String providerRuc) {
        if (minimumStock < 0 || unitPrice.signum() < 0) throw new InvalidMaterialDataException("Negative price or minimum stock");
        this.name = name;
        this.unit = unit;
        this.minimumStock = minimumStock;
        this.unitPrice = unitPrice;
        this.provider = provider;
        this.providerRuc = providerRuc;
    }

    // Read compatibility for rows created before stock existed; the first write persists the baseline.
    public Integer getStock() {
        return stock != null ? stock : Math.max(0, quantity - (quantityExit == null ? 0 : quantityExit));
    }
    public Integer getMinimumStock() { return minimumStock == null ? 0 : minimumStock; }

    public void enter(int amount, LocalDate occurredOn) {
        if (amount <= 0) throw new InvalidMaterialDataException("Quantity must be positive");
        try {
            int nextQuantity = Math.addExact(quantity, amount);
            int nextStock = Math.addExact(getStock(), amount);
            quantity = nextQuantity;
            stock = nextStock;
        } catch (ArithmeticException ex) {
            throw new InvalidMaterialDataException("Quantity exceeds supported range");
        }
        updateStockDate(occurredOn);
    }

    public void use(int amount, LocalDate occurredOn) {
        if (amount <= 0) throw new InvalidMaterialDataException("Quantity must be positive");
        if (amount > getStock()) throw new InsufficientStockException(id);
        stock = getStock() - amount;
        updateStockDate(occurredOn);
    }

    private void updateStockDate(LocalDate occurredOn) {
        LocalDate previous = date == null ? null : LocalDate.parse(date);
        if (previous == null || occurredOn.isAfter(previous)) date = occurredOn.toString();
    }
}
