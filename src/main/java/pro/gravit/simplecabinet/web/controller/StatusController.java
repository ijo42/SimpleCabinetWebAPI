package pro.gravit.simplecabinet.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.gravit.simplecabinet.web.WebApplication;
import pro.gravit.simplecabinet.web.service.KeyManagementService;

import java.util.Base64;

@RestController
@RequestMapping("/status")
public class StatusController {
    @Autowired
    private KeyManagementService keyManagementService;

    @GetMapping("/publicinfo")
    public PublicStatusInfo getPublicInfo() {
        return new PublicStatusInfo(WebApplication.VERSION, Base64.getEncoder().encodeToString(keyManagementService.getPublicKey().getEncoded()));
    }

    public static record PublicStatusInfo(String version, String jwtPublicKey) {
    }
}
