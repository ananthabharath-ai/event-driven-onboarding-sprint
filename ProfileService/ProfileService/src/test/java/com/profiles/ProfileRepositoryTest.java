package com.profiles;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.profile.config.DynamoDbTestContainerConfig;
import com.profiles.model.UserProfile;
import com.profiles.repositories.ProfileRepository;
import com.profiles.services.ProfileService;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;

@SpringBootTest
// Ensure the config that launches the container is loaded
@Import(DynamoDbTestContainerConfig.class)
@ActiveProfiles("test")
public class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;
    
    @Autowired
    private DynamoDbClient dynamoDbClient; 

    @MockBean
    private ProfileService profileService;

    private static final String TABLE_NAME = "UserProfile";

    /**
     * CRITICAL FIX: Dynamically injects the correct AWS properties 
     * using the actual port assigned by Testcontainers, 
     * overriding the hardcoded 'localhost:50660' endpoint.
     */
    @DynamicPropertySource
    static void setDynamoDbProperties(DynamicPropertyRegistry registry) {
        // Dynamically configures the AWS SDK client endpoint
        registry.add("aws.dynamodb.endpoint", () -> String.format("http://%s:%d", 
            DynamoDbTestContainerConfig.dynamodb.getHost(),
            DynamoDbTestContainerConfig.dynamodb.getMappedPort(8000)));
        
        // AWS SDK often requires a region, even for local DynamoDB
        registry.add("aws.region", () -> "us-east-1");
    }
    
    /**
     * This runs once before any tests in this class to ensure the table is created
     * before Spring attempts to instantiate the ProfileRepository.
     */
    @BeforeAll
    static void setupOnce(@Autowired DynamoDbClient dynamoDbClient) {
        try {
            // 1. Delete the table if it exists
            if (dynamoDbClient.listTables().tableNames().contains(TABLE_NAME)) {
                dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName(TABLE_NAME).build());
            }

            // 2. Wait for table to be deleted
            dynamoDbClient.waiter().waitUntilTableNotExists(
                DescribeTableRequest.builder().tableName(TABLE_NAME).build()
            );

            // 3. Create the table
            DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(dynamoDbClient)
                    .build();
            enhancedClient.table(TABLE_NAME, TableSchema.fromBean(UserProfile.class)).createTable();

            // 4. Wait for table to be active
            dynamoDbClient.waiter().waitUntilTableExists(
                DescribeTableRequest.builder().tableName(TABLE_NAME).build()
            );
        } catch (Exception e) {
            System.err.println("DynamoDB Test Setup failed: " + e.getMessage());
            // Fail fast if setup fails
            throw new RuntimeException("Failed to set up DynamoDB table for testing.", e);
        }
    }

    @Test
    void whenSaveProfile_thenFindById_ReturnsSameProfile() {
        // 1. Arrange
        UserProfile newProfile = new UserProfile("user-123", "test@example.com", "My bio");

        // 2. Act
        profileRepository.save(newProfile);
        
        // 3. Assert
        UserProfile foundProfile = profileRepository.findById("user-123");

        assertThat(foundProfile).isNotNull();
        assertThat(foundProfile.getUserId()).isEqualTo("user-123");
        assertThat(foundProfile.getEmail()).isEqualTo("test@example.com");
    }
}