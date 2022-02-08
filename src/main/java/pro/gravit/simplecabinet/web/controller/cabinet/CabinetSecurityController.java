package pro.gravit.simplecabinet.web.controller.cabinet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.exception.InvalidParametersException;
import pro.gravit.simplecabinet.web.service.PasswordCheckService;
import pro.gravit.simplecabinet.web.service.UserService;

@RestController
@CrossOrigin()
@RequestMapping("/cabinet/security")
public class CabinetSecurityController {
    @Autowired
    private PasswordCheckService passwordCheckService;
    @Autowired
    private UserService userService;

    @PostMapping("/changepassword")
    public void changePassword(@RequestBody ChangePasswordRequest request) {
        var user = userService.getCurrentUser();
        var ref = user.getReference();
        if (!passwordCheckService.checkPassword(ref, request.oldPassword)) {
            throw new InvalidParametersException("Old password wrong", 10);
        }
        passwordCheckService.setPassword(ref, request.newPassword);
        userService.save(ref);
    }

    @PostMapping("/prepare2fa")
    @Transactional
    public Prepare2FAResponse prepare2FA() {
        var user = userService.getCurrentUser().getReference();
        if (user.getTotpSecretKey() != null) {
            throw new IllegalArgumentException("2FA already enabled");
        }
        var secret = passwordCheckService.newTotpSecret();
        return new Prepare2FAResponse(secret, passwordCheckService.makeQrCodeUri(user, secret));
    }

    @PostMapping("/enable2fa")
    @Transactional
    public void enable2FA(@RequestBody Enable2FARequest request) {
        var user = userService.getCurrentUser().getReference();
        if (user.getTotpSecretKey() != null) {
            throw new IllegalArgumentException("2FA already enabled");
        }
        var secret = request.secret;
        if (!passwordCheckService.checkTotpPassword(secret, request.code)) {
            throw new IllegalArgumentException("Code incorrect");
        }
        user.setTotpSecretKey(secret);
        userService.save(user);
    }

    @PostMapping("/disable2fa")
    @Transactional
    public void disable2FA(@RequestBody Disable2FARequest request) {
        var user = userService.getCurrentUser().getReference();
        if (user.getTotpSecretKey() == null) {
            throw new IllegalArgumentException("2FA already disabled");
        }
        if (!passwordCheckService.checkTotpPassword(user, request.code)) {
            throw new IllegalArgumentException("Code incorrect");
        }
        user.setTotpSecretKey(null);
        userService.save(user);
    }

    public static record ChangePasswordRequest(String oldPassword, String newPassword) {

    }

    public static record Enable2FARequest(String secret, String code) {

    }

    public static record Disable2FARequest(String code) {

    }

    public static record Prepare2FAResponse(String secret, String uri) {

    }
}
