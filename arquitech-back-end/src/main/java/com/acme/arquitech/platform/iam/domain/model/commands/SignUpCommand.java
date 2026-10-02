package com.acme.arquitech.platform.iam.domain.model.commands;
import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
public record SignUpCommand(String fullName, String email, String password, Role role, String profilePicture, String phone) {}
