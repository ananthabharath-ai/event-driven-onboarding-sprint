package com.profiles.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean 
public class UserProfile {
	
    private String userId; 
    
    private String email;
    private String bio;
    
    @DynamoDbPartitionKey
    public String getUserId() {
        return this.userId;
    }

}