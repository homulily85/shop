package com.shop.client.vault;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.webclient.WebClient;

import java.util.HashMap;
import java.util.Map;

public class VaultApiClient {
    private final WebClient webClient = WebClient.getInstance();
    private final String SERVER_VAULT_URL = System.getenv("SERVER_VAULT_URL");
    private final String VAULT_TOKEN = System.getenv("VAULT_TOKEN");
    private final ObjectMapper objectMapper = new ObjectMapper();

    private VaultApiClient() {

    }

    public static VaultApiClient getInstance() {
        return Holder.INSTANCE;
    }

    public Map<String, String> getSecrets() {
        String url = SERVER_VAULT_URL + "/v1/secret/data/shop";

        try {
            String responseBody = webClient.get(url, Map.of("Accept", "application/json",
                    "X-Vault-Token", VAULT_TOKEN));

            var responseJson = objectMapper.readTree(responseBody);
            var dataNode = responseJson.get("data").get("data");

            Map<String, String> secrets = new HashMap<>();
            dataNode.fieldNames().forEachRemaining(field -> secrets.put(field, dataNode.get(field).asText()));

            return secrets;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final class Holder {
        private static final VaultApiClient INSTANCE = new VaultApiClient();
    }
}
