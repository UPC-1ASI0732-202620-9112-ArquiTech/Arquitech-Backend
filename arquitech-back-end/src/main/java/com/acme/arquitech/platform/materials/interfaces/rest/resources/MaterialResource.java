package com.acme.arquitech.platform.materials.interfaces.rest.resources;
import com.acme.arquitech.platform.materials.domain.model.aggregates.Material;
import java.math.BigDecimal;
import java.time.LocalDate;
public record MaterialResource(Long id, Long projectId, String name, String unit, BigDecimal quantity,
        BigDecimal stock, BigDecimal minimumStock, BigDecimal unitPrice, String provider, String providerRuc, LocalDate date) {
    public static MaterialResource from(Material m) {
        return new MaterialResource(m.getId(), m.getProjectId(), m.getName(), m.getUnit(), m.getQuantity(),
                m.getStock(), m.getMinimumStock(), m.getUnitPrice(), m.getProvider(), m.getProviderRuc(),
                m.getDate() == null ? null : LocalDate.parse(m.getDate()));
    }
}
