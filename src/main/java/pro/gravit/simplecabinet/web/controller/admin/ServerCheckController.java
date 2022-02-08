package pro.gravit.simplecabinet.web.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.configuration.jwt.JwtProvider;
import pro.gravit.simplecabinet.web.dto.UserDto;
import pro.gravit.simplecabinet.web.exception.InvalidParametersException;
import pro.gravit.simplecabinet.web.service.DtoService;
import pro.gravit.simplecabinet.web.service.SessionService;
import pro.gravit.simplecabinet.web.service.UserService;

@RestController
@CrossOrigin()
@RequestMapping("/admin/server")
public class ServerCheckController {
    @Autowired
    public UserService userService;
    @Autowired
    public SessionService sessionService;
    @Autowired
    public DtoService dtoService;
    @Autowired
    public JwtProvider jwtProvider;

    @PostMapping("/joinserver")
    public JoinServerResponse joinServer(@RequestBody JoinServerRequest request) {
        var sessionOptional = sessionService.findById(Long.valueOf(request.sessionId));
        if (sessionOptional.isEmpty()) {
            throw new InvalidParametersException("Session not found", 5);
        }
        var session = sessionOptional.get();
        session.setServerId(request.serverID);
        sessionService.save(session);
        return new JoinServerResponse(true);
    }

    @PostMapping("/publicjoinserver")
    public JoinServerResponse publicJoinServer(@RequestBody JoinServerPublicRequest request) {
        var details = jwtProvider.getDetailsFromToken(request.accessToken);
        if (!details.getUsername().equals(request.username)) {
            return new JoinServerResponse(false);
        }
        var sessionOptional = sessionService.findById(details.getSessionId());
        if (sessionOptional.isEmpty()) {
            throw new InvalidParametersException("Session not found", 5);
        }
        var session = sessionOptional.get();
        session.setServerId(request.serverID);
        sessionService.save(session);
        return new JoinServerResponse(true);
    }

    @PostMapping("/checkserver")
    @Transactional
    public UserDto checkServer(@RequestBody CheckServerRequest request) {
        var userOptional = userService.findByUsername(request.username);
        if (userOptional.isEmpty()) {
            throw new InvalidParametersException("User not found", 1);
        }
        var sessionOptional = sessionService.findByUserAndServerId(userOptional.get(), request.serverID);
        if (sessionOptional.isEmpty()) {
            throw new InvalidParametersException("Session not found", 5);
        }
        return dtoService.toPublicUserDto(userOptional.get());
    }

    public static record JoinServerRequest(String sessionId, String serverID) {
    }

    public static record JoinServerPublicRequest(String username, String accessToken, String serverID) {
    }

    public static record JoinServerResponse(boolean success) {
    }

    public static record CheckServerRequest(String username, String serverID) {
    }
}
