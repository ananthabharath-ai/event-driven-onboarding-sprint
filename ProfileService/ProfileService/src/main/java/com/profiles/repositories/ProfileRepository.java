package com.profiles.repositories;

import org.springframework.stereotype.Repository;

import com.profiles.model.UserProfile;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;

@Repository
public class ProfileRepository {

    private final DynamoDbTable<UserProfile> profileTable;
    private final DynamoDbClient dynamoDbClient;
    private static final String TABLE_NAME = "UserProfile";

    public ProfileRepository(DynamoDbEnhancedClient enhancedClient, DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
        this.profileTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(UserProfile.class));
        
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        if (!dynamoDbClient.listTables().tableNames().contains(TABLE_NAME)) {
            profileTable.createTable();
            dynamoDbClient.waiter().waitUntilTableExists(
                DescribeTableRequest.builder()
                        .tableName(TABLE_NAME)
                        .build()
            );
        }
    }

    /**
     * Saves a UserProfile to DynamoDB.
     *
     * [THE FIX]: This method now returns the saved UserProfile
     * to match the service layer's requirements.
     */
    public UserProfile save(UserProfile profile) {
        profileTable.putItem(profile);
        return profile; // <-- This is the fix
    }

    /**
     * Finds a UserProfile by its partition key (userId).
     */
    public UserProfile findById(String userId) {
        return profileTable.getItem(r -> r.key(k -> k.partitionValue(userId)));
    }
}