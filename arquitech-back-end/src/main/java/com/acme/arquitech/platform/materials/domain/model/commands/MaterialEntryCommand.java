package com.acme.arquitech.platform.materials.domain.model.commands;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

public record MaterialEntryCommand(BigDecimal quantity, String supplier, OffsetDateTime occurredAt, String note) { }
