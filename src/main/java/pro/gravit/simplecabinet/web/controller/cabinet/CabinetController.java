package pro.gravit.simplecabinet.web.controller.cabinet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pro.gravit.simplecabinet.web.exception.InvalidParametersException;
import pro.gravit.simplecabinet.web.model.User;
import pro.gravit.simplecabinet.web.service.SkinService;
import pro.gravit.simplecabinet.web.service.UserService;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/cabinet")
public class CabinetController {
    @Autowired
    public SkinService skinService;
    @Autowired
    public UserService userService;

    @PostMapping("/upload/skin")
    public void uploadSkin(@RequestParam("variant") String variant, @RequestParam("file") MultipartFile file) throws IOException {
        var user = userService.getCurrentUser();
        var ref = user.getReference();
        var path = skinService.getSkinPath(user);
        var limits = skinService.getSkinLimits(user);
        if (!skinService.checkLimitsPre(file, limits)) {
            throw new InvalidParametersException("File too large", 7);
        }
        file.transferTo(path);
        if (!skinService.checkLimitsPost(path, limits)) {
            Files.delete(path);
            throw new InvalidParametersException("File height or width exceeds the limit", 8);
        }
        ref.setSkinModel(variant);
        userService.save(ref);
    }

    @PostMapping("/upload/cloak")
    public void uploadCloak(@RequestParam("file") MultipartFile file) throws IOException {
        var user = userService.getCurrentUser();
        var path = skinService.getCloakPath(user);
        var limits = skinService.getCloakLimits(user);
        if (!skinService.checkLimitsPre(file, limits)) {
            throw new InvalidParametersException("File too large", 7);
        }
        file.transferTo(path);
        if (!skinService.checkLimitsPost(path, limits)) {
            Files.delete(path);
            throw new InvalidParametersException("File height or width exceeds the limit", 8);
        }
    }


    @DeleteMapping("/upload/skin")
    public void deleteSkin() throws IOException {
        var user = userService.getCurrentUser();
        var path = skinService.getSkinPath(user);
        Files.deleteIfExists(path);
    }

    @DeleteMapping("/upload/cloak")
    public void deleteCloak() throws IOException {
        var user = userService.getCurrentUser();
        var path = skinService.getCloakPath(user);
        Files.deleteIfExists(path);
    }

    @PostMapping("/setstatus")
    public void setStatus(@RequestBody SetStatusRequest request) {
        var user = userService.getCurrentUser();
        var ref = user.getReference();
        ref.setStatus(request.status);
        userService.save(ref);
    }

    @PostMapping("/setgender")
    public void setGender(@RequestBody SetGenderRequest request) {
        var user = userService.getCurrentUser();
        var ref = user.getReference();
        ref.setGender(request.gender);
        userService.save(ref);
    }

    @PostMapping("/setskinmodel")
    public void setSkinModel(@RequestBody SetSkinModelRequest request) {
        var user = userService.getCurrentUser();
        var ref = user.getReference();
        ref.setSkinModel(request.model);
        userService.save(ref);
    }

    public static record SetStatusRequest(String status) {
    }

    public static record SetGenderRequest(User.Gender gender) {
    }

    public static record SetSkinModelRequest(String model) {

    }
}
