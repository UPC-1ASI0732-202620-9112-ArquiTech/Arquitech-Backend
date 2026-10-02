package com.acme.arquitech.platform.materials.domain.model.commands;
import java.time.OffsetDateTime;

public record MaterialUsageCommand(Integer quantity, OffsetDateTime occurredAt, String note) { }
