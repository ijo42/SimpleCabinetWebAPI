package pro.gravit.simplecabinet.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.dto.BanInfoDto;
import pro.gravit.simplecabinet.web.dto.PageDto;
import pro.gravit.simplecabinet.web.exception.EntityNotFoundException;
import pro.gravit.simplecabinet.web.service.BanService;

@RestController
@CrossOrigin()
@RequestMapping("/banlist")
public class BanlistController {
    @Autowired
    private BanService banService;

    @GetMapping("/userId/{userId}")
    public BanInfoDto getById(@PathVariable long id) {
        var optional = banService.findById(id);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("BanInfo not found");
        }
        return new BanInfoDto(optional.get());
    }

    @GetMapping("/page/{pageId}")
    public PageDto<BanInfoDto> getPage(@PathVariable int pageId) {
        var list = banService.findAll(PageRequest.of(pageId, 10));
        return new PageDto<>(list.map(BanInfoDto::new));
    }
}
