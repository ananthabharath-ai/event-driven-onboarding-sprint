package com.profiles.repositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.profiles.model.UserProfile;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@RequiredArgsConstructor
public class ProfileRepository {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private DynamoDbTable<UserProfile> profileTable;
    private static final String TABLE_NAME = "UserProfile";

    @Value("${spring.profiles.active:}")
    private String activeProfile;

 
    @PostConstruct
    public void init() {
    	profileTable = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(UserProfile.class));
    	 // Create table if it does not exist
        try {
            profileTable.createTable();
            System.out.println("✅ DynamoDB table created: " + TABLE_NAME);
        } catch (Exception e) {
            System.out.println("ℹ️ Table already exists or cannot be created: " + e.getMessage());
        }
     }
  
    public UserProfile save(UserProfile profile) {
        profileTable.putItem(profile);
        System.out.println("✅ Saved profile: " + profile.getUserId());
        return profile;
    }

    public UserProfile findById(String userId) {
        return profileTable.getItem(r -> r.key(k -> k.partitionValue(userId)));
    }
}