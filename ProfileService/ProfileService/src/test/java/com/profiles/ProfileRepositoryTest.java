package com.profiles;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

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
@Import(DynamoDbTestContainerConfig.class)
@ActiveProfiles("test")
public class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;
    
    @Autowired
    private DynamoDbClient dynamoDbClient; // For setup

    @MockBean
    private ProfileService profileService;

    // This must match the table name in the ProfileRepository
    private static final String TABLE_NAME = "UserProfile";

    // Re-create the table before each test
    @BeforeEach
    void setUp() {
         // Delete the table if it exists
        if (dynamoDbClient.listTables().tableNames().contains(TABLE_NAME)) {
            dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName(TABLE_NAME).build());
        }

        // Wait for table to be deleted
        dynamoDbClient.waiter().waitUntilTableNotExists(
            DescribeTableRequest.builder().tableName(TABLE_NAME).build()
        );

        // Create the table
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        enhancedClient.table(TABLE_NAME, TableSchema.fromBean(UserProfile.class)).createTable();

        // Wait for table to be active
        dynamoDbClient.waiter().waitUntilTableExists(
            DescribeTableRequest.builder().tableName(TABLE_NAME).build()
        );
    }

    @Test
    void whenSaveProfile_thenFindById_ReturnsSameProfile() {
        // 1. Arrange
        UserProfile newProfile = new UserProfile("user-123", "test@example.com", "My bio");

        // 2. Act
        profileRepository.save(newProfile);
        
        // [THE FIX] The findById method returns a UserProfile directly, not an Optional.
        UserProfile foundProfile = profileRepository.findById("user-123");

        // 3. Assert
        assertThat(foundProfile).isNotNull();
        assertThat(foundProfile.getUserId()).isEqualTo("user-123");
        assertThat(foundProfile.getEmail()).isEqualTo("test@example.com");
    }
}