package com.notification;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.KafkaEvent;
import com.notification.handler.NotificationHandler;

/**
 * [RED] Test for the NotificationHandler.
 * This is a pure JUnit 5 test, no Spring needed.
 */
@ExtendWith(MockitoExtension.class) // This initializes all the @Mock and @InjectMocks
public class NotificationHandlerTest {

    // 1. Mock the AWS-provided dependencies
    @Mock
    private Context mockContext;
    
    @Mock
    private LambdaLogger mockLogger;

    @Mock
    private KafkaEvent mockEvent;

    // 2. Inject the mocks into our handler
    @InjectMocks
    private NotificationHandler notificationHandler;

    @BeforeEach
    void setUp() {
        // 3. Arrange: When the handler asks for the logger, return our mock logger.
        when(mockContext.getLogger()).thenReturn(mockLogger);
    }

    @Test
    void testHandleRequest_shouldLogMessage() {
        // 4. Arrange: Create a "fake" Kafka event with one record
        KafkaEvent.KafkaEventRecord mockRecord = new KafkaEvent.KafkaEventRecord();
        mockRecord.setValue("{\"email\":\"test@example.com\"}"); // A simple JSON string payload
        
        Map<String, List<KafkaEvent.KafkaEventRecord>> recordsMap = Map.of(
            "user-created-topic", List.of(mockRecord)
        );
        
        when(mockEvent.getRecords()).thenReturn(recordsMap);

        // 5. Act: Call the handler
        notificationHandler.handleRequest(mockEvent, mockContext);

        // 6. Assert: Verify that the logger's "log" method was called AT LEAST once.
        //
        //    THIS IS THE ASSERTION THAT WILL FAIL.
        //    Our handler's handleRequest() method is empty, so log() is never called.
        //
        verify(mockLogger, atLeastOnce()).log(anyString());
    }
}