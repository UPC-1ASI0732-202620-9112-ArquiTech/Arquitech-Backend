package com.acme.arquitech.platform.iam.application.internal.queryservices;
import com.acme.arquitech.platform.iam.application.internal.authorization.CurrentUserService;
import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import com.acme.arquitech.platform.iam.domain.services.UserQueryService;
import com.acme.arquitech.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;
    private final CurrentUserService currentUser;
    public User getById(Long id) {
        if (currentUser.get().getRole() != Role.SUPERVISOR) currentUser.requireSelf(id);
        return userRepository.findById(id).orElseThrow(() -> ApiException.notFound("NOT_FOUND", "User was not found"));
    }
    public List<User> getAll() {
        currentUser.supervisor();
        return userRepository.findAll();
    }
}
