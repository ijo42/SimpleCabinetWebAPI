package pro.gravit.simplecabinet.web.controller.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.dto.GroupOrderDto;
import pro.gravit.simplecabinet.web.dto.GroupProductDto;
import pro.gravit.simplecabinet.web.dto.PageDto;
import pro.gravit.simplecabinet.web.exception.EntityNotFoundException;
import pro.gravit.simplecabinet.web.exception.InvalidParametersException;
import pro.gravit.simplecabinet.web.model.GroupProduct;
import pro.gravit.simplecabinet.web.service.UserService;
import pro.gravit.simplecabinet.web.service.shop.GroupProductService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/shop/group/")
public class GroupShopController {
    @Autowired
    private GroupProductService groupProductService;
    @Autowired
    private UserService userService;

    @GetMapping("/page/{pageId}")
    public PageDto<GroupProductDto> getPage(@PathVariable int pageId) {
        var list = groupProductService.findAllAvailable(PageRequest.of(pageId, 10));
        return new PageDto<>(list.map(GroupProductDto::new));
    }

    @GetMapping("/id/{id}")
    public GroupProductDto getById(@PathVariable long id) {
        var optional = groupProductService.findById(id);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("GroupProduct not found");
        }
        return new GroupProductDto(optional.get());
    }

    @PostMapping("/buy")
    @PreAuthorize("isAuthenticated()")
    public GroupOrderDto buyGroup(@RequestBody BuyGroupRequest request) {
        var product = groupProductService.findById(request.id);
        if (product.isEmpty()) {
            throw new InvalidParametersException("Product not found", 1);
        }
        var user = userService.getCurrentUser();
        var order = groupProductService.createGroupOrder(product.get(), request.quantity, user.getReference());
        groupProductService.delivery(order);
        return new GroupOrderDto(order);
    }

    @PutMapping("/new")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GroupProductDto create(@RequestBody GroupProductCreateRequest request) {
        var product = new GroupProduct();
        product.setDisplayName(request.displayName);
        product.setDescription(request.description);
        product.setName(request.name);
        product.setServer(request.server);
        product.setWorld(request.world);
        product.setContext(request.context);
        product.setExpireDays(request.expireDays);
        product.setLocal(request.local);
        product.setPrice(request.price);
        product.setCurrency(request.currency);
        product.setStackable(request.stackable);
        product.setLocalName(request.localName);
        product.setAvailable(false);
        groupProductService.save(product);
        return new GroupProductDto(product);
    }

    @PostMapping("/id/{id}/setlimitations")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void setLimitations(@PathVariable long id, @RequestBody SetLimitationsRequest request) {
        var optional = groupProductService.findById(id);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("GroupProduct not found");
        }
        var product = optional.get();
        product.setEndDate(request.endDate);
        product.setCount(request.count);
        product.setGroupName(request.groupName);
        groupProductService.save(product);
    }

    @PostMapping("/id/{id}/setavailable")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void setAvailable(@PathVariable long id, @RequestBody SetAvailableRequest request) {
        var optional = groupProductService.findById(id);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("GroupProduct not found");
        }
        var product = optional.get();
        product.setAvailable(request.available);
        groupProductService.save(product);
    }

    @PostMapping("/id/{id}/setprice")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void setPrice(@PathVariable long id, @RequestBody SetPriceRequest request) {
        var optional = groupProductService.findById(id);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("GroupProduct not found");
        }
        var product = optional.get();
        product.setPrice(request.price);
        groupProductService.save(product);
    }

    @PostMapping("/id/{id}/update")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void update(@PathVariable long id, @RequestBody GroupProductUpdateRequest request) {
        var optional = groupProductService.findById(id);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("GroupProduct not found");
        }
        var product = optional.get();
        product.setDisplayName(request.displayName);
        product.setDescription(request.description);
        groupProductService.save(product);
    }

    public static record BuyGroupRequest(long id, int quantity) {
    }

    public record GroupProductCreateRequest(String displayName, String description, String name, String server,
                                            String world, String context, long expireDays, boolean local, double price,
                                            String currency, boolean stackable, String localName) {
    }

    public record SetAvailableRequest(boolean available) {
    }

    public record SetPriceRequest(double price) {
    }

    public record GroupProductUpdateRequest(String displayName, String description) {
    }

    public record SetLimitationsRequest(LocalDateTime endDate, long count, String groupName) {
    }
}
