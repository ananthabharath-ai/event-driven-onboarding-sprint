package com.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.user.model.User;
import com.user.repositories.UserRepository;
import com.user.services.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
	
	// 1. @Mock creates a fake UserRepository
	@Mock
	private UserRepository userRepo;
	
	// 2. We now ALSO need to mock the KafkaTemplate for day4
	@Mock
	private KafkaTemplate<String,Object>kafkaTemplate;
	
	@InjectMocks
	private UserServiceImpl userServiceImpl;
	
	private static final String topicName = "user-created-topic-cloud";
	
	@Test
	void whenCreateUser_shouldSaveToRepository() {
		
		//1. arrange the users
		User userToSave = new User("bharath","bharath01@gmail.com");
		User userWithId = new User("1","bharath","bharath01@gmail.com");
		
		 // Stub the mock repository's save method
		when(userRepo.save(any(User.class))).thenReturn(userWithId);
		
        User savedUser = userServiceImpl.createUser(userToSave);
       
        verify(userRepo).save(any(User.class));

        // We can also verify that the kafkaTemplate.send() method was called!
        verify(kafkaTemplate).send(eq(topicName), any(String.class), any(User.class));
    
        // Also assert that the correct object was returned
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isEqualTo("1"); // Check the String ID
        assertThat(savedUser.getName()).isEqualTo("bharath");
        assertThat(savedUser.getEmail()).isEqualTo("bharath01@gmail.com"); // Fixed assertion data
	}
}