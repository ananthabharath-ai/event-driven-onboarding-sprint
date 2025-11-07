package com.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.controllers.UserController;
import com.user.model.User;
import com.user.services.UserService; //we will mock this

@WebMvcTest(UserController.class)
public class UserControllerTest {
	
	// MockMvc is our main tool for performing mock HTTP requests.
	@Autowired
	private MockMvc mockMvc;
	
	//ObjectMapper is used to serialize out User object into a JSON string
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean
	private UserService userService;
	
	@Test
	void whenPostUser_thenReturns201Created() throws Exception {
		
		// 1. Create our test user objects
		User userToCreate = new User("bharath","bharath01@gmail.com");
		User savedUser = new User("1","bharath","bharath01@gmail.com");
		
		//2. When userService.createUser is called with ANY User object, then return our 'savedUser' object
		when(userService.createUser(any(User.class))).thenReturn(savedUser);
		
		//3. Perform the POST request
		mockMvc.perform(post("/users")
			   .contentType(MediaType.APPLICATION_JSON)
			   .content(objectMapper.writeValueAsString(userToCreate)))
			
		// 4. Assert: Check the response status and body
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value("1"))
				.andExpect(jsonPath("$.name").value("bharath"));
		
		//5.verify if the userService is called
		verify(userService).createUser(any(User.class));
	}
}