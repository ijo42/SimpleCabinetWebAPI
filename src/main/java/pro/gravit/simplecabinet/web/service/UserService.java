package pro.gravit.simplecabinet.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import pro.gravit.simplecabinet.web.exception.InvalidParametersException;
import pro.gravit.simplecabinet.web.model.BasicUser;
import pro.gravit.simplecabinet.web.model.User;
import pro.gravit.simplecabinet.web.repository.UserRepository;
import pro.gravit.simplecabinet.web.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserService {
    private final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");
    @Autowired
    private UserRepository repository;
    @Autowired
    private PasswordCheckService passwordCheckService;

    public <S extends User> S save(S entity) {
        return repository.save(entity);
    }


    public Optional<User> findById(Long aLong) {
        return repository.findById(aLong);
    }

    public Optional<User> findByUsername(String username) {
        return repository.findByUsernameIgnoreCase(username);
    }

    public Optional<User> findByUsernameOrEmail(String username, String email) {
        return repository.findByUsernameIgnoreCaseOrEmailIgnoreCaseAllIgnoreCase(username, email);
    }

    public Optional<User> findByUUID(UUID uuid) {
        return repository.findByUuid(uuid);
    }

    public Page<User> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public User getReference(Long aLong) {
        return repository.getById(aLong);
    }

    public void delete(User entity) {
        repository.delete(entity);
    }

    public User register(String username, String email, String password) {
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new InvalidParametersException("Username contains forbidden characters", 33);
        }
        if (!User.isCorrectEmail(email)) {
            throw new InvalidParametersException("Email not in correct format", 34);
        }
        User user = new User();
        user.setUsername(username);
        user.setUuid(UUID.randomUUID());
        user.setEmail(email);
        passwordCheckService.setPassword(user, password);
        user.setRegistrationDate(LocalDateTime.now());
        user.setGroups(new ArrayList<>());
        repository.save(user);
        return user;
    }

    public CurrentUser getCurrentUser() {
        var details = SecurityUtils.getUser();
        return new CurrentUser(details);
    }

    public class CurrentUser implements BasicUser {
        private final UserDetailsService.CabinetUserDetails details;
        private User user;

        public CurrentUser(UserDetailsService.CabinetUserDetails details) {
            this.details = details;
        }

        public User getReference() {
            if (user == null) {
                user = repository.getById(getId());
            }
            return user;
        }

        public String getClient() {
            return details.getClient();
        }

        public long getSessionId() {
            return details.getSessionId();
        }

        public String getPermission(String key) {
            var map = details.getPermissions();
            return map.get(key);
        }

        public Collection<? extends GrantedAuthority> getAuthorities() {
            return details.getAuthorities();
        }

        @Override
        public long getId() {
            return details.getUserId();
        }

        public String getUsername() {
            return details.getUsername();
        }

        @Override
        public UUID getUuid() {
            return null;
        }
    }
}
