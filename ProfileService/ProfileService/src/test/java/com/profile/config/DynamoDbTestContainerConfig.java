package com.profile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Profile("test")
public class DynamoDbTestContainerConfig {

    private static final String DYNAMODB_IMAGE = "amazon/dynamodb-local:latest";
    private static final int DYNAMODB_PORT = 8000;

    // Static container ensures it starts before Spring beans are created
    public static final GenericContainer<?> dynamodb = new GenericContainer<>(DockerImageName.parse(DYNAMODB_IMAGE))
            .withExposedPorts(DYNAMODB_PORT)
            .withCommand("-jar", "DynamoDBLocal.jar", "-sharedDb", "-inMemory");

    static {
        dynamodb.start(); // important: start container before Spring tries to get mapped port
    }

    @Bean
    @Primary
    public DynamoDbClient dynamoDbClient() throws URISyntaxException {
        String endpointUrl = String.format("http://%s:%d",
                dynamodb.getHost(),
                dynamodb.getMappedPort(DYNAMODB_PORT));

        return DynamoDbClient.builder()
                .endpointOverride(new URI(endpointUrl))
                .region(Region.of("ap-south-1"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("dummy", "dummy")
                ))
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}