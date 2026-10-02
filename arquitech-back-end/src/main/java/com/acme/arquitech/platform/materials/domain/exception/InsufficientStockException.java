package com.acme.arquitech.platform.materials.domain.exception;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import org.springframework.http.HttpStatus;
public class InsufficientStockException extends ApiException {
    public InsufficientStockException(Long materialId) {
        super(HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK", "Insufficient stock");
    }
}
