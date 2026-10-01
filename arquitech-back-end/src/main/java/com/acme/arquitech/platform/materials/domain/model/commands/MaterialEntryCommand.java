package com.acme.arquitech.platform.materials.domain.model.commands;
import java.time.OffsetDateTime;

public record MaterialEntryCommand(Integer quantity, String supplier, OffsetDateTime occurredAt, String note) { }
