package com.profiles.repositories;

import org.springframework.stereotype.Repository;

import com.profiles.model.UserProfile;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class ProfileRepository {

    private final DynamoDbTable<UserProfile> profileTable;

    /**
     * This constructor is where the test will fail.
     * When Spring tries to build this bean, TableSchema.fromBean() will
     * fail because UserProfile is not a valid DynamoDB entity.
     * This causes an UnsatisfiedDependencyException in our test.
     */
    public ProfileRepository(DynamoDbEnhancedClient enhancedClient) {
        this.profileTable = enhancedClient.table("user_profiles", TableSchema.fromBean(UserProfile.class));
    }

    public UserProfile save(UserProfile profile) {
        profileTable.putItem(profile);
        return profile;
    }

    public UserProfile findById(String userId) {
        return profileTable.getItem(Key.builder().partitionValue(userId).build());
    }
    
    // Helper method for testing to create the table
    public void createTable() {
        profileTable.createTable();
    }
}