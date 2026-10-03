package com.acme.arquitech.platform.materials.domain.model.commands;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

public record MaterialUsageCommand(BigDecimal quantity, OffsetDateTime occurredAt, String note) { }
