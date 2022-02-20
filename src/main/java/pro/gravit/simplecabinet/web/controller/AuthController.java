package pro.gravit.simplecabinet.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.configuration.jwt.JwtProvider;
import pro.gravit.simplecabinet.web.dto.UserDto;
import pro.gravit.simplecabinet.web.exception.AuthException;
import pro.gravit.simplecabinet.web.exception.EntityNotFoundException;
import pro.gravit.simplecabinet.web.exception.RegisterException;
import pro.gravit.simplecabinet.web.model.User;
import pro.gravit.simplecabinet.web.service.DtoService;
import pro.gravit.simplecabinet.web.service.PasswordCheckService;
import pro.gravit.simplecabinet.web.service.SessionService;
import pro.gravit.simplecabinet.web.service.UserService;
import pro.gravit.simplecabinet.web.utils.SecurityUtils;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordCheckService passwordCheckService;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private DtoService dtoService;

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        var optional = userService.findByUsernameOrEmail(request.username, request.email);
        if (optional.isPresent()) {
            throw new RegisterException("User already registered", 3);
        }
        User user = userService.register(request.username, request.email, request.password);
        return new RegisterResponse(user.getId());
    }

    @PostMapping("/authorize")
    public ResponseEntity<AuthResponse> auth(@RequestBody AuthRequest request) {
        var optional = userService.findByUsername(request.username);
        if (optional.isEmpty()) {
            throw new AuthException("User not found", 3);
        }
        var user = optional.get();
        if (user.getBanInfo() != null) {
            throw new AuthException("User banned", 4);
        }
        var success = passwordCheckService.checkPassword(user, request.password);
        if (!success) {
            throw new AuthException("Password not correct", 5);
        }
        if (user.getTotpSecretKey() != null) {
            if (request.totpPassword == null) {
                throw new AuthException("auth.require2fa", 7);
            }
            if (!passwordCheckService.checkTotpPassword(user, request.totpPassword)) {
                throw new AuthException("2FA Password not correct", 6);
            }
        }
        var session = sessionService.create(user, "Basic");
        var token = jwtProvider.generateToken(session);
        HttpCookie cookie = ResponseCookie.from("session", token.token())
                .path("/")
                .sameSite("Strict")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", cookie.toString());
        return new ResponseEntity<>(new AuthResponse(token.token(), session.getRefreshToken(), token.getExpire()), headers, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(@RequestBody RefreshTokenRequest request) {
        var session = sessionService.findByRefreshToken(request.refreshToken);
        if (session.isEmpty()) {
            throw new AuthException("Session not found");
        }
        var sessionObject = session.get();
        if (sessionObject.isDeleted()) {
            throw new AuthException("Session not found");
        }
        sessionService.update(sessionObject);
        var token = jwtProvider.generateToken(sessionObject);
        return new AuthResponse(token.token(), sessionObject.getRefreshToken(), token.getExpire());
    }

    @GetMapping("/userinfo")
    @PreAuthorize("isAuthenticated()")
    public UserDto getUserInfo() {
        var details = SecurityUtils.getUser();
        var userOptional = userService.findById(details.getUserId());
        if (userOptional.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        return dtoService.toPublicUserDto(userOptional.get());
    }

    public static record RegisterResponse(long id) {
    }

    public static record RegisterRequest(String username, String email, String password) {
    }

    public static record AuthRequest(String username, String password, String totpPassword) {
    }

    public static record AuthResponse(String accessToken, String refreshToken, long expire) {
    }

    public static record RefreshTokenRequest(String refreshToken) {

    }
}
