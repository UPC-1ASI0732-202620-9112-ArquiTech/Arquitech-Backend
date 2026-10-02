package com.acme.arquitech.platform.projects.domain.exceptions;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import org.springframework.http.HttpStatus;
public class ProjectNotFoundException extends ApiException {
    public ProjectNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "Project with id " + id + " was not found");
    }
}
