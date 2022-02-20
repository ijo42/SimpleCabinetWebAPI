package pro.gravit.simplecabinet.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.dto.PageDto;
import pro.gravit.simplecabinet.web.dto.UserDto;
import pro.gravit.simplecabinet.web.exception.EntityNotFoundException;
import pro.gravit.simplecabinet.web.service.DtoService;
import pro.gravit.simplecabinet.web.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class
UserController {
    @Autowired
    private UserService service;
    @Autowired
    private DtoService dtoService;

    @GetMapping("/id/{userId}")
    public UserDto getById(@PathVariable long userId) {
        var optional = service.findById(userId);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        return dtoService.toPublicUserDto(optional.get());
    }

    @DeleteMapping("/id/{userId}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteStatus(@PathVariable long userId) {
        var optional = service.findById(userId);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        var user = optional.get();
        user.setStatus(null);
        service.save(user);
    }

    @DeleteMapping("/id/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteUserById(@PathVariable long userId) {
        var optional = service.findById(userId);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        service.delete(optional.get());
    }

    @GetMapping("/name/{name}")
    public UserDto getByUsername(@PathVariable String name) {
        var optional = service.findByUsername(name);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        return dtoService.toPublicUserDto(optional.get());
    }

    @GetMapping("/uuid/{uuid}")
    public UserDto getByUUID(@PathVariable UUID uuid) {
        var optional = service.findByUUID(uuid);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        return dtoService.toPublicUserDto(optional.get());
    }

    @GetMapping("/page/{pageId}")
    public PageDto<UserDto> getPage(@PathVariable int pageId) {
        var list = service.findAll(PageRequest.of(pageId, 10));
        return new PageDto<>(list.map(dtoService::toMiniUserDto));
    }
}
