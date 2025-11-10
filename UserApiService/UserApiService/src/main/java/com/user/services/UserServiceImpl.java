package com.user.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.user.model.User;
import com.user.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	private static final String topicName = "user-created-topic-cloud";

    private final KafkaTemplate<String, User> kafkaTemplate;
    private final UserRepository userRepo;
   
    @Autowired
    public UserServiceImpl(KafkaTemplate<String, User> kafkaTemplate, UserRepository userRepo) {
        this.kafkaTemplate = kafkaTemplate;
        this.userRepo = userRepo;
    }
    
	@Override
	public User createUser(User user) {
		
		//1. store the record to the database
		User savedUser = userRepo.save(user);
		
		//2. send the kafka json to kafka topic
		kafkaTemplate.send(topicName,savedUser.getId(), savedUser);
		return savedUser;
	}
}