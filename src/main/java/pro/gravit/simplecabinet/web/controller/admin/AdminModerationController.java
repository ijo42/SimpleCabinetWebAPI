package pro.gravit.simplecabinet.web.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.dto.BanInfoDto;
import pro.gravit.simplecabinet.web.exception.EntityNotFoundException;
import pro.gravit.simplecabinet.web.exception.InvalidParametersException;
import pro.gravit.simplecabinet.web.service.BanService;
import pro.gravit.simplecabinet.web.service.UserService;

@RestController
@RequestMapping("/admin/moderation")
public class AdminModerationController {
    @Autowired
    private UserService userService;
    @Autowired
    private BanService banService;

    @PostMapping("/ban/{userId}")
    public BanInfoDto banUser(@PathVariable long userId, @RequestBody BanRequest request) {
        var user = userService.findById(userId);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        var moderator = userService.getCurrentUser();
        var banInfo = banService.ban(user.get(), moderator.getReference(), request.reason, request.expireMinutes);
        return new BanInfoDto(banInfo);
    }

    @PostMapping("/unban/{userId}")
    public void unbanUser(@PathVariable long userId) {
        var user = userService.findById(userId);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        var banInfo = user.get().getBanInfo();
        if (banInfo == null) {
            throw new InvalidParametersException("User not banned", 5);
        }
        banService.unban(banInfo);
    }

    public static record BanRequest(String reason, long expireMinutes, boolean isHardware) {
    }
}
