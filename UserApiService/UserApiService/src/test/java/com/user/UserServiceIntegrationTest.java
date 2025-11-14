package com.user;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import com.user.model.User;
import com.user.repositories.UserRepository;
import com.user.services.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;



@SpringBootTest
@EmbeddedKafka(partitions = 1,topics = "user-created-topic-cloud")
public class UserServiceIntegrationTest {

	private static final String topicName = "user-created-topic-cloud";
	
	//we will call the actual service
	@Autowired
	private UserService userService;
	
	// We mock the repository, as we are not testing the database here so we will mock the repo
	@MockBean
	private UserRepository userRepository;
	
	// we have embedded in memory kafka broker for testing, injected so we can get its properties
	@Autowired
	private EmbeddedKafkaBroker embeddedKafka;
	
	 // A test-side consumer
	private Consumer<String,User>testConsumer;
	
	@BeforeEach
	void setUp() {
		
		// 1. Get the broker properties
		Map<String,Object>consumerProps = KafkaTestUtils.consumerProps("test-group","true", embeddedKafka);
		
		// 2. Set up our test consumer to read User objects
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class);
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,JsonDeserializer.class);
		consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES,"*");
		consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE,User.class);
		
		 // 3. Create and subscribe the consumer
		DefaultKafkaConsumerFactory<String,User>consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
		testConsumer = consumerFactory.createConsumer();
		testConsumer.subscribe(Collections.singleton(topicName));
	}
	
	@AfterEach
	void cleanUp() {
		if(testConsumer != null) {
			testConsumer.close();
		}
	}
	
	@Test
	void whenCreateUser_thenPublishesKafkaEvent() {
		
		//1. Arrange the users one to take request and one to save and send
		User userToSave = new User("testKafka","testKafka@gmail.com");
		User savedUser = new User("1","testKafka","testKafka@gmail.com");
		
		//2. Mock the database save
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		
		//3. call the real service
		userService.createUser(userToSave);
		
		//4. Assert (Consume the message) Poll the embedded Kafka topic for up to 10 seconds.
		ConsumerRecord<String,User>record = KafkaTestUtils.getSingleRecord(testConsumer, topicName,Duration.ofSeconds(10));
		
	    //5. This is the assertion that will FAIL because we are not sending User to kafka in the service
		assertNotNull(record);
		
        //6. These assertions will be checked once the test passes
		User recievedUser = record.value();
		assertThat(recievedUser.getName()).isEqualTo("testKafka");
		assertThat(recievedUser.getEmail()).isEqualTo("testKafka@gmail.com");
		
	}
}