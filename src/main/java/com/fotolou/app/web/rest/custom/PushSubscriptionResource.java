package com.fotolou.app.web.rest.custom;

import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.security.SecurityUtils;
import com.fotolou.app.service.custom.push.BrowserPushService;
import com.fotolou.app.service.custom.push.PushSubscriptionRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push")
public class PushSubscriptionResource {

    private final BrowserPushService browserPushService;
    private final UserRepository userRepository;

    public PushSubscriptionResource(BrowserPushService browserPushService, UserRepository userRepository) {
        this.browserPushService = browserPushService;
        this.userRepository = userRepository;
    }

    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        String publicKey = browserPushService.getPublicKey();
        return ResponseEntity.ok(Map.of("publicKey", publicKey != null ? publicKey : ""));
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<Void> registerSubscription(@Valid @RequestBody PushSubscriptionRequest request) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> browserPushService.registerSubscription(user, request));
        return ResponseEntity.ok().build();
    }
}
