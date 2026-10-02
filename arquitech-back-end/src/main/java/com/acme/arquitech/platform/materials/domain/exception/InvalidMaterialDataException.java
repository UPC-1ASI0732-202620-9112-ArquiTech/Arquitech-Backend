package com.acme.arquitech.platform.materials.domain.exception;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import org.springframework.http.HttpStatus;
public class InvalidMaterialDataException extends ApiException {
    public InvalidMaterialDataException(String message) {
        super(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }
}
