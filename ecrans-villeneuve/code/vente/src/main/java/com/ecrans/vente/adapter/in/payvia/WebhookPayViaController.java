package com.ecrans.vente.adapter.in.payvia;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
public class WebhookPayViaController {
 private final WebhookPayVia webhook;
 public WebhookPayViaController(WebhookPayVia webhook) { this.webhook = webhook; }
 @PostMapping("/webhooks/payvia")
 @ResponseStatus(HttpStatus.NO_CONTENT)
 public void recevoir(@RequestBody NotificationPayVia notification) { webhook.recevoir(notification); }
}
