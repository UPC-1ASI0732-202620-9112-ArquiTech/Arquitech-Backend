package com.acme.arquitech.platform.machinery.domain.exception;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import org.springframework.http.HttpStatus;
public class MachineryNotFoundException extends ApiException {
    public MachineryNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "MACHINERY_NOT_FOUND", "Machinery with id " + id + " was not found");
    }
}
