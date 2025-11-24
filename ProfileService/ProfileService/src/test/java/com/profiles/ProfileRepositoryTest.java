package com.profiles;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // allows non-static @BeforeAll with @Autowired
public class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @MockBean
    private ProfileService profileService;

    private static final String TABLE_NAME = "UserProfile";

    @BeforeAll
    void setupOnce() {
        try {
            // Delete table if exists
            if (dynamoDbClient.listTables().tableNames().contains(TABLE_NAME)) {
                dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName(TABLE_NAME).build());
            }

            // Wait for table deletion
            dynamoDbClient.waiter().waitUntilTableNotExists(
                    DescribeTableRequest.builder().tableName(TABLE_NAME).build()
            );

            // Create table
            DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(dynamoDbClient)
                    .build();
            enhancedClient.table(TABLE_NAME, TableSchema.fromBean(UserProfile.class)).createTable();

            // Wait for table to be active
            dynamoDbClient.waiter().waitUntilTableExists(
                    DescribeTableRequest.builder().tableName(TABLE_NAME).build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up DynamoDB table for testing.", e);
        }
    }

    @Test
    void whenSaveProfile_thenFindById_ReturnsSameProfile() {
        UserProfile newProfile = new UserProfile("user-123", "test@example.com", "My bio");

        profileRepository.save(newProfile);

        UserProfile foundProfile = profileRepository.findById("user-123");

        assertThat(foundProfile).isNotNull();
        assertThat(foundProfile.getUserId()).isEqualTo("user-123");
        assertThat(foundProfile.getEmail()).isEqualTo("test@example.com");
    }
}