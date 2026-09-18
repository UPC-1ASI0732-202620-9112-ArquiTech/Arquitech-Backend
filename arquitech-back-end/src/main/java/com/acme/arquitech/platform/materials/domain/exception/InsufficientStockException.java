package com.acme.arquitech.platform.materials.domain.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long materialId) {
        super("Insufficient stock for material ID " + materialId);
    }
}