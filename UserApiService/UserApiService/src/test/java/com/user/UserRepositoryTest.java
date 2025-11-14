package com.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.user.model.User;
import com.user.repositories.UserRepository;

//1. @DataMongoTest: Loads only the Spring Data MongoDB parts of the application.
//2. @Testcontainers: Enables Test containers support for JUnit 5.
@DataMongoTest
@Testcontainers
public class UserRepositoryTest {
	
//3.  @Container: Creates a static MongoDB container for all tests in this class.
//    It will be started before tests run and destroyed after.
    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0.4"));

//4.  @DynamicPropertySource: This is crucial. It intercepts the Spring context and dynamically 
//    sets the MongoDB connection URI to point to our randomly-assigned container port.
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    
    // 5. @Autowired: Spring will inject the real UserRepository interface for us to test.
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void whenSaveUser_thenFindById_ReturnsSameUser(){
    	//1. Create the user
    	User userTosave = new User("bharath","bharath01@gmail.com");
    	
    	//2. This save operation will fail as we have not annotated with @Document
        User savedUser = userRepository.save(userTosave);
        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
       
        //3. Assert
        assertAll(
        		()->assertThat(foundUser).isNotNull(),
        		()->assertThat(foundUser.getName()).isEqualTo("bharath"),
        		()->assertThat(foundUser.getEmail()).isEqualTo("bharath01@gmail.com")
        		);
    }
}