package com.profile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.net.URISyntaxException;

@Testcontainers
@Configuration
@Profile("test") 
public class DynamoDbTestContainerConfig {

    private static final String DYNAMODB_IMAGE = "amazon/dynamodb-local:latest";
    private static final int DYNAMODB_PORT = 8000;

    // The DynamoDB container, managed by Testcontainers
    @Container
    public static final GenericContainer<?> dynamodb = new GenericContainer<>(DockerImageName.parse(DYNAMODB_IMAGE))
            .withExposedPorts(DYNAMODB_PORT)
            .withCommand("-jar", "DynamoDBLocal.jar", "-sharedDb", "-inMemory");

    // Start the container
    static {
        dynamodb.start();
    }

    /**
     * Creates the DynamoDbClient using the dynamically mapped host and port 
     * from the running Testcontainer.
     * This fixes the "Connection refused" error caused by hardcoded localhost connections.
     */
    @Bean
    @Primary
    public DynamoDbClient dynamoDbClient() throws URISyntaxException {
        // Dynamically get the host and port assigned by Docker/Testcontainers
        String endpointUrl = String.format("http://%s:%d", dynamodb.getHost(), dynamodb.getMappedPort(DYNAMODB_PORT));

        return DynamoDbClient.builder()
                .endpointOverride(new URI(endpointUrl))
                .region(Region.US_EAST_1)
                // Use dummy credentials for the local DynamoDB instance
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("fakeKey", "fakeSecret")))
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}