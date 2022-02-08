package pro.gravit.simplecabinet.web.controller.payment;

import com.qiwi.billpayments.sdk.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.service.payment.QiwiPaymentService;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin()
@RequestMapping("/webhooks/qiwi")
public class QiwiWebhookController {
    @Autowired
    private QiwiPaymentService service;

    @PostMapping("/payment")
    public void payment(@RequestBody Notification notification, HttpServletRequest httpServletRequest) {
        String signature = httpServletRequest.getHeader("X-Api-Signature-SHA256");
        service.complete(notification, signature);
    }
}
