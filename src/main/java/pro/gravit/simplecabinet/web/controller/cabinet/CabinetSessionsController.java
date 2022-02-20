package pro.gravit.simplecabinet.web.controller.cabinet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.dto.PageDto;
import pro.gravit.simplecabinet.web.dto.UserSessionDto;
import pro.gravit.simplecabinet.web.exception.EntityNotFoundException;
import pro.gravit.simplecabinet.web.service.SessionService;
import pro.gravit.simplecabinet.web.service.UserService;

@RestController
@RequestMapping("/cabinet/sessions")
public class CabinetSessionsController {
    @Autowired
    private UserService userService;
    @Autowired
    private SessionService sessionService;

    @GetMapping("/page/{pageId}")
    public PageDto<UserSessionDto> getPage(@PathVariable int pageId) {
        var user = userService.getCurrentUser();
        var list = sessionService.findByUserPublic(user.getReference(), PageRequest.of(pageId, 10));
        return new PageDto<>(list.map(UserSessionDto::new));
    }

    @GetMapping("/current")
    public UserSessionDto getCurrentSession() {
        var user = userService.getCurrentUser();
        var session = sessionService.findById(user.getSessionId());
        if (session.isEmpty()) {
            throw new EntityNotFoundException("Session not found");
        }
        return new UserSessionDto(session.get());
    }

    @GetMapping("/id/{id}")
    public UserSessionDto getById(@PathVariable long id) {
        var user = userService.getCurrentUser();
        var optional = sessionService.findById(id);
        if (optional.isEmpty() || optional.get().isDeleted()) {
            throw new EntityNotFoundException("Session not found");
        }
        var session = optional.get();
        if (session.getUser().getId() != user.getId()) {
            throw new SecurityException("Access denied");
        }
        return new UserSessionDto(session);
    }

    @DeleteMapping("/id/{id}")
    public void deleteById(@PathVariable long id) {
        var user = userService.getCurrentUser();
        var optional = sessionService.findById(id);
        if (optional.isEmpty() || optional.get().isDeleted()) {
            throw new EntityNotFoundException("Session not found");
        }
        var session = optional.get();
        if (session.getUser().getId() != user.getId()) {
            throw new SecurityException("Access denied");
        }
        if (session.getId() == user.getSessionId()) {
            throw new SecurityException("You can't delete current session");
        }
        session.setDeleted(true);
        sessionService.save(session);
    }
}
