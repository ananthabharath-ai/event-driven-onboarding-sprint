package com.notification.handler;

import java.util.Base64;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KafkaEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class NotificationHandler implements RequestHandler<KafkaEvent, Void> {

    private static final String SERVICE_URL = "http://profiles-service.internal.local:8081/profiles";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEFAULT_BIO = "This is my new profile.";

    @Override
    public Void handleRequest(KafkaEvent kafkaEvent, Context context) {
        LambdaLogger logger = context.getLogger();

        if (kafkaEvent == null || kafkaEvent.getRecords() == null) {
            logger.log("No records received.");
            return null;
        }

        kafkaEvent.getRecords().forEach((topic, records) -> records.forEach(record -> {
            try {
                // Decode Kafka message
                String decoded = new String(Base64.getDecoder().decode(record.getValue()));
                logger.log("Topic: " + topic + " | Decoded Message: " + decoded);

                // Build JSON payload
                JsonNode node = objectMapper.readTree(decoded);
                ObjectNode payload = objectMapper.createObjectNode()
                        .put("userId", node.get("id").asText())
                        .put("email", node.get("email").asText())
                        .put("bio", DEFAULT_BIO);

                // Inline POST request
                HttpURLConnection conn = (HttpURLConnection) new URL(SERVICE_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(objectMapper.writeValueAsBytes(payload));
                }
                logger.log("POST Response Code: " + conn.getResponseCode());
                conn.disconnect();

            } catch (Exception e) {
                logger.log("Error processing record: " + e.toString());
                
                for (StackTraceElement element : e.getStackTrace()) {
                    logger.log(element.toString() + "\n");
                }
            }
        }));
        return null;
    }
}