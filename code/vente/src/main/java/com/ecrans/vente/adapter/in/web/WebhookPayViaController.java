package com.ecrans.vente.adapter.in.web;

import com.ecrans.vente.adapter.in.payvia.NotificationPayVia;
import com.ecrans.vente.adapter.in.payvia.WebhookPayVia;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks/payvia")
public class WebhookPayViaController {

    private final WebhookPayVia webhookPayVia;

    public WebhookPayViaController(WebhookPayVia webhookPayVia) {
        this.webhookPayVia = webhookPayVia;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recevoir(@RequestBody NotificationPayVia notification) {
        webhookPayVia.recevoir(notification);
    }
}
