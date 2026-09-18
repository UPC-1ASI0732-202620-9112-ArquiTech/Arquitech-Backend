package com.acme.arquitech.platform.machinery.interfaces.rest.resources;

import com.acme.arquitech.platform.machinery.domain.model.valueobjects.MachineryStatus;
import java.util.Date;

public record CreateMachineryResource(
        Long projectId,
        String name,
        String licensePlate,
        Date registerDate,
        MachineryStatus status
) {
}