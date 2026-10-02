package com.acme.arquitech.platform.iam.domain.services;
import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import java.util.List;
public interface UserQueryService {
    User getById(Long id);
    List<User> getAll();
}
