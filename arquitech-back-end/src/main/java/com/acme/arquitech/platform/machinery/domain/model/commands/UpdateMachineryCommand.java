package com.acme.arquitech.platform.machinery.domain.model.commands;
import com.acme.arquitech.platform.machinery.domain.model.valueobjects.MachineryStatus;
import java.time.LocalDate;

public record UpdateMachineryCommand(Long projectId, String name, String serialNumber, LocalDate registeredAt, MachineryStatus status, String description) { }
