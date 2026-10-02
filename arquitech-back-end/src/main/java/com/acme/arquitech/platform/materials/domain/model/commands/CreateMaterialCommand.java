package com.acme.arquitech.platform.materials.domain.model.commands;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateMaterialCommand(Long projectId, String name, String unit, BigDecimal quantity, BigDecimal minimumStock, BigDecimal unitPrice, String provider, String providerRuc, LocalDate date) { }
