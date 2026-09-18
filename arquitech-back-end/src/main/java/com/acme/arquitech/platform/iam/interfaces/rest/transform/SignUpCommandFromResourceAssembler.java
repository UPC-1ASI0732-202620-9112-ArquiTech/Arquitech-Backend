package com.acme.arquitech.platform.iam.interfaces.rest.transform;

import com.acme.arquitech.platform.iam.domain.model.commands.SignUpCommand;
import com.acme.arquitech.platform.iam.interfaces.rest.resources.SignUpResource;

public class SignUpCommandFromResourceAssembler {
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        return new SignUpCommand(resource.name(), resource.email(), resource.password(), resource.role(), resource.profilePicture());
    }
}