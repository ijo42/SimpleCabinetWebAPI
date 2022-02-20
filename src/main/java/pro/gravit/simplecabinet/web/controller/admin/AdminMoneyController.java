package pro.gravit.simplecabinet.web.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.dto.BalanceTransactionDto;
import pro.gravit.simplecabinet.web.dto.UserBalanceDto;
import pro.gravit.simplecabinet.web.exception.BalanceException;
import pro.gravit.simplecabinet.web.exception.EntityNotFoundException;
import pro.gravit.simplecabinet.web.model.User;
import pro.gravit.simplecabinet.web.service.BalanceService;
import pro.gravit.simplecabinet.web.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/admin/money")
public class AdminMoneyController {
    @Autowired
    private UserService userService;
    @Autowired
    private BalanceService balanceService;

    @GetMapping("/userbalance/id/{id}")
    public UserBalanceDto getUserBalanceById(@PathVariable long id) {
        var balance = balanceService.findById(id);
        if (balance.isEmpty()) {
            throw new EntityNotFoundException("Balance not found");
        }
        return new UserBalanceDto(balance.get());
    }

    @GetMapping("/userbalance/userid/{userId}/{currency}")
    public UserBalanceDto getUserBalanceByUserIdAndCurrency(@PathVariable long userId, @PathVariable String currency) {
        var userOptional = userService.findById(userId);
        if (userOptional.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        var user = userOptional.get();
        var balance = balanceService.findOrCreateUserBalanceByUserAndCurrency(user, currency);
        return new UserBalanceDto(balance);
    }

    @GetMapping("/userbalance/uuid/{userUuid}/{currency}")
    public UserBalanceDto getUserBalanceByUuidAndCurrency(@PathVariable UUID userUuid, @PathVariable String currency) {
        var userOptional = userService.findByUUID(userUuid);
        if (userOptional.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        var user = userOptional.get();
        var balance = balanceService.findOrCreateUserBalanceByUserAndCurrency(user, currency);
        return new UserBalanceDto(balance);
    }

    @PostMapping("/addmoney/unchecked/{balanceId}")
    public BalanceTransactionDto addMoneyUnchecked(@PathVariable long balanceId, @RequestBody AddMoneyRequest request) throws BalanceException {
        var transaction = balanceService.addMoney(balanceId, request.count, request.comment);
        return new BalanceTransactionDto(transaction);
    }

    @PostMapping("/addmoney/byuuid/{userUUID}/{currency}")
    public BalanceTransactionDto addMoneyByUUID(@PathVariable UUID userUUID, @PathVariable String currency, @RequestBody AddMoneyRequest request) throws BalanceException {
        var user = userService.findByUUID(userUUID);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        var balanceTo = balanceService.findOrCreateUserBalanceByUserAndCurrency(user.get(), currency);
        var transaction = balanceService.addMoney(balanceTo.getId(), request.count, request.comment);
        return new BalanceTransactionDto(transaction);
    }

    @PostMapping("/addmoney/byid/{userId}/{currency}")
    public BalanceTransactionDto addMoneyByUserId(@PathVariable long userId, @PathVariable String currency, @RequestBody AddMoneyRequest request) throws BalanceException {
        var user = userService.getReference(userId);
        var balanceTo = balanceService.findOrCreateUserBalanceByUserAndCurrency(user, currency);
        var transaction = balanceService.addMoney(balanceTo.getId(), request.count, request.comment);
        return new BalanceTransactionDto(transaction);
    }

    @PostMapping("/removemoney/unchecked/{balanceId}")
    public BalanceTransactionDto removeMoneyUnchecked(@PathVariable long balanceId, @RequestBody AddMoneyRequest request) throws BalanceException {
        var transaction = balanceService.removeMoney(balanceId, request.count, request.comment);
        return new BalanceTransactionDto(transaction);
    }

    @PostMapping("/removemoney/byuuid/{userUUID}/{currency}")
    public BalanceTransactionDto removeMoneyByUUID(@PathVariable UUID userUUID, @PathVariable String currency, @RequestBody AddMoneyRequest request) throws BalanceException {
        var user = userService.findByUUID(userUUID);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        var balanceTo = balanceService.findOrCreateUserBalanceByUserAndCurrency(user.get(), currency);
        var transaction = balanceService.removeMoney(balanceTo.getId(), request.count, request.comment);
        return new BalanceTransactionDto(transaction);
    }

    @PostMapping("/removemoney/byid/{userId}/{currency}")
    public BalanceTransactionDto removeMoneyByUserId(@PathVariable long userId, @PathVariable String currency, @RequestBody AddMoneyRequest request) throws BalanceException {
        var user = userService.getReference(userId);
        var balanceTo = balanceService.findOrCreateUserBalanceByUserAndCurrency(user, currency);
        var transaction = balanceService.removeMoney(balanceTo.getId(), request.count, request.comment);
        return new BalanceTransactionDto(transaction);
    }

    @PostMapping("/transfer/byuuid/{fromUUID}/{fromCurrency}/to/{toUUID}/{toCurrency}")
    public BalanceTransactionDto transferByUUID(@PathVariable UUID fromUUID, @PathVariable String fromCurrency, @PathVariable UUID toUUID, @PathVariable String toCurrency, @RequestBody TransferMoneyRequest request) throws BalanceException {
        var fromUserOptional = userService.findByUUID(fromUUID);
        if (fromUserOptional.isEmpty()) {
            throw new EntityNotFoundException("From User not found");
        }
        var toUserOptional = userService.findByUUID(toUUID);
        if (toUserOptional.isEmpty()) {
            throw new EntityNotFoundException("To User not found");
        }
        var fromBalance = balanceService.findOrCreateUserBalanceByUserAndCurrency(fromUserOptional.get(), fromCurrency);
        var toBalance = balanceService.findOrCreateUserBalanceByUserAndCurrency(toUserOptional.get(), toCurrency);
        User user;
        if (request.selfUser) {
            user = fromBalance.getUser();
        } else {
            user = null;
        }
        var transaction = balanceService.transfer(user == null ? null : user.getId(),
                fromBalance.getId(), toBalance.getId(), fromBalance.getCurrency(), toBalance.getCurrency(), request.count, request.comment, request.strictRate);
        return new BalanceTransactionDto(transaction);
    }

    @PostMapping("/transfer/byid/{fromUserId}/{fromCurrency}/to/{toUserId}/{toCurrency}")
    public BalanceTransactionDto transferByUserId(@PathVariable long fromUserId, @PathVariable String fromCurrency, @PathVariable long toUserId, @PathVariable String toCurrency, @RequestBody TransferMoneyRequest request) throws BalanceException {
        var fromUser = userService.getReference(fromUserId);
        var toUser = userService.getReference(toUserId);
        var fromBalance = balanceService.findOrCreateUserBalanceByUserAndCurrency(fromUser, fromCurrency);
        var toBalance = balanceService.findOrCreateUserBalanceByUserAndCurrency(toUser, toCurrency);
        User user;
        if (request.selfUser) {
            user = fromBalance.getUser();
        } else {
            user = null;
        }
        var transaction = balanceService.transfer(user == null ? null : user.getId(),
                fromBalance.getId(), toBalance.getId(), fromBalance.getCurrency(), toBalance.getCurrency(), request.count, request.comment, request.strictRate);
        return new BalanceTransactionDto(transaction);
    }

    @PostMapping("/transfer/unchecked/multicurrency/{userId}/from/{fromId}/{fromCurrency}/to/{toId}/{toCurrency}")
    public BalanceTransactionDto transferUncheckedMultiCurrency(@PathVariable Long userId, @PathVariable Long fromId, @PathVariable String fromCurrency, @PathVariable Long toId, @PathVariable String toCurrency, @RequestBody TransferMoneyRequest request) throws BalanceException {
        var transaction = balanceService.transfer(userId,
                fromId, toId, fromCurrency, toCurrency, request.count, request.comment, request.strictRate);
        return new BalanceTransactionDto(transaction);
    }

    @PostMapping("/transfer/unchecked/nocurrency/{userId}/from/{fromId}/to/{toId}")
    public BalanceTransactionDto transferUncheckedNoCurrency(@PathVariable Long userId, @PathVariable Long fromId, @PathVariable Long toId, @RequestBody TransferMoneyRequest request) throws BalanceException {
        var transaction = balanceService.transfer(userId,
                fromId, toId, request.count, request.count, false, request.comment);
        return new BalanceTransactionDto(transaction);
    }

    public static record AddMoneyRequest(double count, String comment) {

    }

    public static record TransferMoneyRequest(double count, boolean selfUser, String comment, boolean strictRate) {
    }
}
