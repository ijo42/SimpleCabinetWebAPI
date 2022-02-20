package pro.gravit.simplecabinet.web.controller.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.service.payment.YooPaymentService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/webhooks/yoo")
public class YooWebhookController {
    @Autowired
    private YooPaymentService service;

    private static boolean matches(String ip, String subnet) {
        IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(subnet);
        return ipAddressMatcher.matches(ip);
    }

    @PostMapping("/payment")
    public void payment(@RequestBody YooPaymentService.YooPaymentNotification notification, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (!matches(ip, "185.71.76.0/27") && !matches(ip, "185.71.77.0/27") && !matches(ip, "77.75.153.0/25") && !matches(ip, "77.75.154.128/25") &&
                !matches(ip, "2a02:5180:0:1509::/64") && !matches(ip, "2a02:5180:0:2655::/64") && !matches(ip, "2a02:5180:0:1533::/64") &&
                !matches(ip, "2a02:5180:0:2669::/64")) {
            throw new SecurityException("Access denied (IP address)");
        }
        service.complete(notification);
    }
}
