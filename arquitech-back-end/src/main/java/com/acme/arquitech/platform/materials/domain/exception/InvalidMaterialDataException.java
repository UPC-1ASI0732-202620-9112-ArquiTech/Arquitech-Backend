package com.acme.arquitech.platform.materials.domain.exception;

public class InvalidMaterialDataException extends RuntimeException {
    public InvalidMaterialDataException(String message) {
        super(message);
    }
}