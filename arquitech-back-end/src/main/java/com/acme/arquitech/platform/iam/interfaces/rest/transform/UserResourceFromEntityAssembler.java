package com.acme.arquitech.platform.iam.interfaces.rest.transform;

import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import com.acme.arquitech.platform.iam.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {
    public static UserResource toResourceFromEntity(User entity) {
        return new UserResource(entity.getId(), entity.getName(), entity.getEmail(), entity.getRole(), entity.getProfilePicture());
    }
}