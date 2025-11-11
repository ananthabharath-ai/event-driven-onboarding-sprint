package com.profiles.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

/**
 * This is the "real" configuration for the application.
 * It replaces what DynamoDbTestContainerConfig was doing for our tests.
 * * It reads the DynamoDB URL from our new application.properties file
 * (which gets its value from the docker-compose.yml environment variable)
 * and builds the real DynamoDbClient bean.
 */
@Configuration
public class DynamoDbConfig {

    // Read the 'aws.dynamodb.endpoint' property from application.properties
    @Value("${aws.dynamodb.endpoint}")
    private String dynamoDbEndpoint;

    // Read the 'aws.region' property
    @Value("${aws.region}")
    private String awsRegion;

    /**
     * Creates the DynamoDbClient bean.
     */
    @Bean
    public DynamoDbClient dynamoDbClient() {
        // Our docker-compose.yml sets dummy credentials "test"/"test"
        // This is how we provide them to the client.
        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create("test", "test")
        );

        return DynamoDbClient.builder()
                .endpointOverride(URI.create(dynamoDbEndpoint)) // <-- Connect to our local container
                .region(Region.of(awsRegion))
                .credentialsProvider(credentials)
                .build();
    }

    /**
     * Creates the Enhanced client bean, which our repository needs.
     */
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}