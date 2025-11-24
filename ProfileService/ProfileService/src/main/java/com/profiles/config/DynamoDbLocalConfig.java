package com.profiles.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@Profile("local")
public class DynamoDbLocalConfig {
	
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClientLocal(
            @Value("${AWS_DYNAMODB_ENDPOINT}")  String dynamodbEndpoint
    ) throws URISyntaxException {

        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of("ap-south-1"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("fakeAccessKey","fakeSecretKey")
                ))
                .endpointOverride(new URI(dynamodbEndpoint)) // Required for DynamoDB Local
                .build();

        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}