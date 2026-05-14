package com.shop.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility for HMAC-SHA256 signing of outgoing inter-service HTTP requests.
 * The receiving PHP gateway validates the signature.
 */
public class HmacUtil {
    private static final String ALGORITHM = "HmacSHA256";

    private static final String SECRET;

    static {
        // Try Vault first, fall back to env var
        String vaultSecret = null;
        try {
            var vaultService = com.shop.service.VaultService.getInstance();
            vaultSecret = vaultService.getSecret("INTERNAL_API_SECRET");
        } catch (Exception e) {
            // Vault not available
        }

        if (vaultSecret != null && !vaultSecret.isBlank()) {
            SECRET = vaultSecret;
        } else {
            String env = System.getenv("INTERNAL_API_SECRET");
            if (env == null || env.isBlank()) {
                System.err.println("[HmacUtil] WARNING: INTERNAL_API_SECRET not set, inter-service auth disabled");
                SECRET = "";
            } else {
                SECRET = env;
            }
        }
    }

    /**
     * Generates an HMAC-SHA256 signature for the given body and timestamp.
     * The signed payload is: timestamp + ":" + body
     */
    public static String sign(String timestamp, String body) {
        try {
            String payload = timestamp + ":" + body;
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC signature", e);
        }
    }
}
