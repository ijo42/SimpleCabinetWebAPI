package pro.gravit.simplecabinet.web.controller.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.dto.ItemOrderDto;
import pro.gravit.simplecabinet.web.dto.ItemProductDto;
import pro.gravit.simplecabinet.web.dto.PageDto;
import pro.gravit.simplecabinet.web.exception.EntityNotFoundException;
import pro.gravit.simplecabinet.web.exception.InvalidParametersException;
import pro.gravit.simplecabinet.web.model.ItemProduct;
import pro.gravit.simplecabinet.web.service.UserService;
import pro.gravit.simplecabinet.web.service.shop.ItemProductService;

@RestController
@RequestMapping("/shop/item/")
public class ItemShopController {
    @Autowired
    private ItemProductService productService;
    @Autowired
    private UserService userService;

    @GetMapping("/page/{pageId}")
    public PageDto<ItemProductDto> getPage(@PathVariable int pageId) {
        var list = productService.findAllAvailable(PageRequest.of(pageId, 10));
        return new PageDto<>(list.map(ItemProductDto::new));
    }

    @GetMapping("/id/{id}")
    public ItemProductDto getById(@PathVariable long id) {
        var optional = productService.findById(id);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("ItemProduct not found");
        }
        return new ItemProductDto(optional.get());
    }

    @PostMapping("/id/{id}/setlimitations")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void setLimitations(@PathVariable long id, @RequestBody GroupShopController.SetLimitationsRequest request) {
        var optional = productService.findById(id);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("GroupProduct not found");
        }
        var product = optional.get();
        product.setEndDate(request.endDate());
        product.setCount(request.count());
        product.setGroupName(request.groupName());
        productService.save(product);
    }

    @PostMapping("/id/{id}/setavailable")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void setAvailable(@PathVariable long id, @RequestBody GroupShopController.SetAvailableRequest request) {
        var optional = productService.findById(id);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("GroupProduct not found");
        }
        var product = optional.get();
        product.setAvailable(request.available());
        productService.save(product);
    }

    @PostMapping("/id/{id}/setprice")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void setPrice(@PathVariable long id, @RequestBody GroupShopController.SetPriceRequest request) {
        var optional = productService.findById(id);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("GroupProduct not found");
        }
        var product = optional.get();
        product.setPrice(request.price());
        productService.save(product);
    }

    @PutMapping("/new")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ItemProductDto create(@RequestBody CreateItemRequest request) {
        ItemProduct product = new ItemProduct();
        product.setDisplayName(request.displayName);
        product.setDescription(request.description);
        product.setPrice(request.price);
        product.setCurrency(request.currency);
        product.setItemName(request.itemName);
        product.setItemExtra(request.itemExtra);
        product.setItemNbt(request.itemNbt);
        product.setItemCustom(request.itemCustom);
        product.setItemQuantity(request.itemQuantity);
        product.setServer(request.server);
        product.setAvailable(false);
        productService.save(product);
        return new ItemProductDto(product);
    }

    @PostMapping("/buy")
    @PreAuthorize("isAuthenticated()")
    public ItemOrderDto buyItem(@RequestBody BuyItemRequest request) {
        var user = userService.getCurrentUser();
        var productOptional = productService.findById(request.id);
        if (productOptional.isEmpty()) {
            throw new InvalidParametersException("ItemProduct not found", 1);
        }
        var product = productOptional.get();
        var order = productService.createItemOrder(product, request.quantity, user.getReference());
        productService.delivery(order);
        return new ItemOrderDto(order);
    }

    public record BuyItemRequest(long id, long quantity) {

    }

    public record CreateItemRequest(String displayName, String description, double price,
                                    String currency, String itemName, String itemExtra, String itemNbt,
                                    String itemCustom, int itemQuantity, String server) {

    }
}
