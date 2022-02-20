package pro.gravit.simplecabinet.web.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.dto.AuditDto;
import pro.gravit.simplecabinet.web.dto.PageDto;
import pro.gravit.simplecabinet.web.exception.EntityNotFoundException;
import pro.gravit.simplecabinet.web.service.AuditService;

@RestController
@RequestMapping("/admin/audit")
public class AuditController {
    @Autowired
    private AuditService service;

    @GetMapping("/id/{id}")
    public AuditDto getById(@PathVariable long id) {
        var optional = service.findById(id);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("Audit not found");
        }
        return new AuditDto(optional.get());
    }

    @GetMapping("/page/{pageId}")
    public PageDto<AuditDto> getPage(@PathVariable int pageId) {
        var list = service.findAll(PageRequest.of(pageId, 10));
        return new PageDto<>(list.map(AuditDto::new));
    }
}
