package sentinel_backend.alert;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import sentinel_backend.event.SecurityEventResponse;

@Component
public class AlertWebhookClient {

    private final RestClient restClient;
    private final String webhookUrl;

    public AlertWebhookClient(
            @Value("${sentinel.alert.webhook-url:}") String webhookUrl) {
        this.restClient = RestClient.builder().build();
        this.webhookUrl = webhookUrl;
    }

    public void sendCriticalAlert(SecurityEventResponse event) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(event)
                .retrieve()
                .toBodilessEntity();
    }
}