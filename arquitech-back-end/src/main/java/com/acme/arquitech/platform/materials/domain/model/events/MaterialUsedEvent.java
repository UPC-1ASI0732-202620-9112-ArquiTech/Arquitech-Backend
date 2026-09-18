package com.acme.arquitech.platform.materials.domain.model.events;

public record MaterialUsedEvent(Long materialId, Integer quantityUsed, String exitDate) {
}