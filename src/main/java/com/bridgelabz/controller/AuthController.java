package com.bridgelabz.controller;

import com.bridgelabz.model.ResponseDTO;
import com.bridgelabz.security.PccoeAccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class AuthController {
    private final PccoeAccessService pccoeAccessService;

    public AuthController(PccoeAccessService pccoeAccessService) {
        this.pccoeAccessService = pccoeAccessService;
    }

    /**
     * Public endpoint to check if user is authenticated.
     * Returns user info if logged in, or authenticated=false if not.
     */
    @GetMapping("/auth/status")
    public ResponseEntity<?> authStatus(OAuth2AuthenticationToken authToken) {
        if (authToken == null) {
            return ResponseEntity.ok(ResponseDTO.success("Not authenticated",
                    Map.of("authenticated", false)));
        }

        try {
            OAuth2User user = authToken.getPrincipal();
            Map<String, Object> userInfo = new LinkedHashMap<>();
            userInfo.put("authenticated", true);
            userInfo.put("name", user.getAttribute("name"));
            userInfo.put("email", user.getAttribute("email"));
            userInfo.put("picture", user.getAttribute("picture"));
            userInfo.put("capabilities", Map.of(
                    "pccoeExport", pccoeAccessService.canAccess(user.getAttribute("email"))));
            return ResponseEntity.ok(ResponseDTO.success("Authenticated", userInfo));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseDTO.success("Not authenticated",
                    Map.of("authenticated", false)));
        }
    }
}
