package com.user.services;

import java.util.regex.Pattern;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.user.model.User;
import com.user.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	private static final String topicName = "user-created-topic-cloud";

	private static final Pattern EMAIL_PATTERN = Pattern.compile(
		        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", 
		        Pattern.CASE_INSENSITIVE
		    );

    private final KafkaTemplate<String, User> kafkaTemplate;
    private final UserRepository userRepo;
   
    
    public UserServiceImpl(KafkaTemplate<String, User> kafkaTemplate, UserRepository userRepo) {
        this.kafkaTemplate = kafkaTemplate;
        this.userRepo = userRepo;
    }
    
	@Override
	public User createUser(User user) {
		
		//0. Validating the user
		validateUser(user);
		
		//1. store the record to the database
		User savedUser = userRepo.save(user);
		
		//2. send the kafka json to kafka topic
		kafkaTemplate.send(topicName,savedUser.getId(), savedUser);
		return savedUser;
	}
	
	  private void validateUser(User user) {
	        if (user == null) {
	            throw new IllegalArgumentException("User cannot be null");
	        }
	        if (user.getName() == null || user.getName().isBlank()) {
	            throw new IllegalArgumentException("User name cannot be null or blank");
	        }
	        if (user.getEmail() == null || !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
	            throw new IllegalArgumentException("User email is invalid");
	        }
	    }
}