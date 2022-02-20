package pro.gravit.simplecabinet.web.controller.cabinet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.dto.BalanceTransactionDto;
import pro.gravit.simplecabinet.web.dto.PageDto;
import pro.gravit.simplecabinet.web.dto.UserBalanceDto;
import pro.gravit.simplecabinet.web.exception.AuthException;
import pro.gravit.simplecabinet.web.exception.BalanceException;
import pro.gravit.simplecabinet.web.exception.EntityNotFoundException;
import pro.gravit.simplecabinet.web.model.UserBalance;
import pro.gravit.simplecabinet.web.service.BalanceService;
import pro.gravit.simplecabinet.web.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/cabinet/money")
public class CabinetMoneyController {
    @Autowired
    private UserService userService;
    @Autowired
    private BalanceService balanceService;

    @GetMapping("/balance/page/{pageId}")
    public PageDto<UserBalanceDto> getBalancePage(@PathVariable int pageId) {
        var user = userService.getCurrentUser();
        var list = balanceService.findUserBalanceByUser(user.getReference(), PageRequest.of(pageId, 10));
        return new PageDto<>(list.map(UserBalanceDto::new));
    }

    @GetMapping("/balance/id/{id}")
    public UserBalanceDto getBalanceById(@PathVariable long id) {
        var user = userService.getCurrentUser();
        var optional = balanceService.findById(id);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("Balance not found");
        }
        if (optional.get().getUser().getId() != user.getId()) {
            throw new AuthException("Access denied");
        }
        return new UserBalanceDto(optional.get());
    }

    @GetMapping("/balance/id/{id}/transactions/page/{pageId}")
    public PageDto<BalanceTransactionDto> getBalanceById(@PathVariable long id, @PathVariable int pageId) {
        var user = userService.getCurrentUser();
        var optional = balanceService.findById(id);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("Balance not found");
        }
        if (optional.get().getUser().getId() != user.getId()) {
            throw new AuthException("Access denied");
        }
        var list = balanceService.findAllByBalance(optional.get(), PageRequest.of(pageId, 10));
        return new PageDto<>(list.map(BalanceTransactionDto::new));
    }

    @GetMapping("/balance/currency/{currency}")
    public UserBalanceDto getBalanceByCurrency(@PathVariable String currency) {
        var user = userService.getCurrentUser();
        var optional = balanceService.findUserBalanceByUserAndCurrency(user.getReference(), currency);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("Balance not found");
        }
        return new UserBalanceDto(optional.get());
    }

    @PostMapping("/transfer/{fromCurrency}/to/{userId}/{toCurrency}")
    @Transactional
    public BalanceTransactionDto transfer(@PathVariable String fromCurrency, @PathVariable long toId, @PathVariable String toCurrency, @RequestBody TransferRequest request) throws BalanceException {
        var user = userService.getCurrentUser();
        var ref = user.getReference();
        var fromBalanceOptional = balanceService.findUserBalanceByUserAndCurrency(ref, fromCurrency);
        if (fromBalanceOptional.isEmpty()) {
            throw new EntityNotFoundException("Your balance not found");
        }
        var targetUser = userService.findById(toId);
        if (targetUser.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        boolean needCreate = fromCurrency.equals(toCurrency);
        Optional<UserBalance> toBalanceOptional;
        if (needCreate) {
            toBalanceOptional = Optional.of(balanceService.findOrCreateUserBalanceByUserAndCurrency(targetUser.get(), toCurrency));
        } else {
            toBalanceOptional = balanceService.findUserBalanceByUserAndCurrency(targetUser.get(), toCurrency);
        }
        if (toBalanceOptional.isEmpty()) {
            throw new EntityNotFoundException("Target balance not found");
        }
        var fromBalance = fromBalanceOptional.get();
        var toBalance = toBalanceOptional.get();
        var transaction = balanceService.transfer(user.getId(), fromBalance.getId(), toBalance.getId(),
                fromBalance.getCurrency(), toBalance.getCurrency(),
                request.count, request.comment, true);
        return new BalanceTransactionDto(transaction);
    }

    public record TransferRequest(long count, String comment) {
    }
}
