package com.acme.arquitech.platform.iam.domain.model.commands;

import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;

import java.util.List;

/**
 * Sign up command
 * <p>
 *     This class represents the command to sign up a user.
 * </p>
 * @param username the username of the user
 * @param password the password of the user
 * @param roles the roles of the user
 *
 * @see com.acme.center.platform.iam.domain.model.aggregates.User
 */
public record SignUpCommand(String name, String email, String password, Role role, String profilePicture) {}
