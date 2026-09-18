package com.acme.arquitech.platform.materials.domain.exception;

public class MaterialNotFoundException extends RuntimeException {
    public MaterialNotFoundException(Long id) {
        super("Material with ID " + id + " not found");
    }
}