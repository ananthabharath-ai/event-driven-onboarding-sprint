package com.notification.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KafkaEvent;

/**
 * [GREEN] FIX: This is the updated Lambda handler.
 * It now gets the logger and logs each incoming message.
 */
public class NotificationHandler implements RequestHandler<KafkaEvent, Void> {

    @Override
    public Void handleRequest(KafkaEvent kafkaEvent, Context context) {
        
        // 1. Get the logger from the Lambda context
        LambdaLogger logger = context.getLogger();

        logger.log("Lambda function invoked. Received " + kafkaEvent.getRecords().size() + " records.");

        // 2. Loop through all topics and records in the event
        for (String topic : kafkaEvent.getRecords().keySet()) {
            for (KafkaEvent.KafkaEventRecord record : kafkaEvent.getRecords().get(topic)) {
                
                // 3. Log the message value!
                // This is the line that makes our [RED] test pass.
                String messageValue = record.getValue();
                logger.log("Received message from topic " + topic + ": " + messageValue);

            }
        }
        return null;
    }
}