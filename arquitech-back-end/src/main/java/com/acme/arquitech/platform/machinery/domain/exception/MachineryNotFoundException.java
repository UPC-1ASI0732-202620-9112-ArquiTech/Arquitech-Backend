package com.acme.arquitech.platform.machinery.domain.exception;

public class MachineryNotFoundException extends RuntimeException {
  public MachineryNotFoundException(Long id) {
    super("Machinery with ID " + id + " not found");
  }
}