package com.acme.arquitech.platform.iam.interfaces.rest.transform;
import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import com.acme.arquitech.platform.iam.interfaces.rest.resources.UserResource;
public class UserResourceFromEntityAssembler {
    public static UserResource toResourceFromEntity(User user) {
        return new UserResource(user.getId(), user.getName(), user.getEmail(), user.getRole(),
                user.getPhone(), user.getCreatedAt(), user.getProfilePicture());
    }
}
