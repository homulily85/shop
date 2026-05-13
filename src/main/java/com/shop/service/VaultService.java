package com.shop.service;

import com.shop.client.vault.VaultApiClient;

import java.util.Map;

public class VaultService {
    private final Map<String, String> secrets;

    private VaultService() {
        VaultApiClient vaultApiClient = VaultApiClient.getInstance();
        secrets = vaultApiClient.getSecrets();
    }

    public static VaultService getInstance() {
        return Holder.INSTANCE;
    }

    public String getSecret(String key) {
        return secrets.get(key);
    }

    private static final class Holder {
        private static final VaultService INSTANCE = new VaultService();
    }
}
