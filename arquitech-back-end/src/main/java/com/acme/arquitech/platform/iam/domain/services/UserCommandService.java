package com.acme.arquitech.platform.iam.domain.services;
import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import com.acme.arquitech.platform.iam.domain.model.commands.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
public interface UserCommandService {
    User handle(SignUpCommand command);
    ImmutablePair<User, String> handle(SignInCommand command);
    User updateProfile(Long id, String fullName, String phone);
}
