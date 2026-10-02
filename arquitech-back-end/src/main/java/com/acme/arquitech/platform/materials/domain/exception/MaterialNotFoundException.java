package com.acme.arquitech.platform.materials.domain.exception;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import org.springframework.http.HttpStatus;
public class MaterialNotFoundException extends ApiException {
    public MaterialNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "MATERIAL_NOT_FOUND", "Material with id " + id + " was not found");
    }
}
