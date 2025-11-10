package com.profile.config;

import java.net.URI;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * This class configures the DynamoDB-local container for our integration tests.
 */
@TestConfiguration
@Testcontainers
public class DynamoDbTestContainerConfig {

    // Note: The artifact is 'dynamodb' but the image is 'dynamodb-local'
    // This uses the official Amazon image.
    @Container
    private static final GenericContainer<?> dynamodb = 
        new GenericContainer<>("amazon/dynamodb-local:latest")
            .withExposedPorts(8000)
            .withCommand("-jar DynamoDBLocal.jar -inMemory -sharedDb");

    static {
        dynamodb.start();
    }

    /**
     * Creates a DynamoDbClient bean that points to the running container.
     * @Primary ensures this bean overrides the default one.
     */
    @Bean
    @Primary
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
            .endpointOverride(URI.create("http://localhost:" + dynamodb.getMappedPort(8000)))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create("test", "test")))
            .region(Region.US_EAST_1)
            .build();
    }

    /**
     * Creates the DynamoDbEnhancedClient bean that our repository needs.
     */
    @Bean
    @Primary
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();
    }
}