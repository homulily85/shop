package com.shop.client.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.service.VaultService;
import com.shop.webclient.WebClient;

import java.util.Map;

public class PaymentApiClient {
    private final WebClient webClient = WebClient.getInstance();
    private final String SERVER_BANK_URL = VaultService.getInstance().getSecret("SERVER_BANK_URL");
    private final ObjectMapper objectMapper = new ObjectMapper();

    private PaymentApiClient() {

    }

    public static PaymentApiClient getInstance() {
        return PaymentApiClient.Holder.INSTANCE;
    }

    public long makePayment(long orderId, long customerId, long amount) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "billId", orderId,
                    "senderId", customerId,
                    "receiverId", 4,
                    "amount", amount
            ));

            String responseBody = webClient.post(SERVER_BANK_URL + "/transfer", Map.of(
                    "Content-Type", "application/json",
                    "Accept", "application/json"
            ), requestBody);

            var responseJson = objectMapper.readTree(responseBody);

            return responseJson.get("data").get("transactions").asLong();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse JSON for payment gateway", e);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("INSUFFICIENT_BALANCE")) {
                throw new IllegalArgumentException("INSUFFICIENT_BALANCE");
            } else if (e.getMessage() != null && e.getMessage().contains("ACCOUNT_NOT_FOUND")) {
                throw new IllegalArgumentException("ACCOUNT_NOT_FOUND");
            } else {
                System.err.println("Payment gateway request failed: " + e.getMessage());
            }

            return -1;
        }
    }

    private static final class Holder {
        private static final PaymentApiClient INSTANCE = new PaymentApiClient();

    }
}