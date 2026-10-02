package com.acme.arquitech.platform.materials.domain.model.commands;
import java.math.BigDecimal;

public record UpdateMaterialCommand(String name, String unit, Integer minimumStock, BigDecimal unitPrice, String provider, String providerRuc) { }
