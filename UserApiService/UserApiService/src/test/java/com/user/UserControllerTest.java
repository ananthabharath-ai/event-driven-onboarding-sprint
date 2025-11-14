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
import com.user.services.UserService;

@WebMvcTest(UserController.class)
public class UserControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean
	private UserService userService;
	
	@Test
	void whenPostUser_thenReturns201Created() throws Exception {
		
		// 1. Arrange
		User userToCreate = new User("bharath","bharath01@gmail.com");
		User savedUser = new User("1","bharath","bharath01@gmail.com");
		
		// 2. Mock
		when(userService.createUser(any(User.class))).thenReturn(savedUser);
		
		// 3. Act & Assert
		mockMvc.perform(post("/users")
			   .contentType(MediaType.APPLICATION_JSON)
			   .content(objectMapper.writeValueAsString(userToCreate)))
			
		// 4. Assert
				.andExpect(status().isCreated()) // Asserts 201
				.andExpect(jsonPath("$.id").value("1"))
				.andExpect(jsonPath("$.name").value("bharath"));
		
		// 5. Verify
		verify(userService).createUser(any(User.class));
	}

	@Test
	void whenPostUser_withInvalidEmail_thenReturns400BadRequest() throws Exception {
		// 1. Arrange
		User invalidUser = new User("Test Name", "not-an-email");
		
		// 2. Mock: Tell the service to throw the exception our controller needs to catch
		when(userService.createUser(any(User.class)))
			.thenThrow(new IllegalArgumentException("User email is invalid"));

		// 3. Act & Assert
		mockMvc.perform(post("/users")
			   .contentType(MediaType.APPLICATION_JSON)
			   .content(objectMapper.writeValueAsString(invalidUser)))
			   
			   // Assert we get a 400 Bad Request
			   .andExpect(status().isBadRequest()) 
			   
			   // Assert the error message is in the body
			   .andExpect(jsonPath("$.message").value("User email is invalid"));
	}

	@Test
	void whenPostUser_withDatabaseError_thenReturns500() throws Exception {
		// 1. Arrange
		User userToCreate = new User("Test User", "test@example.com");
		
		// 2. Mock: Simulate a generic, unexpected error (e.g., database is down)
		when(userService.createUser(any(User.class)))
			.thenThrow(new RuntimeException("Database is down"));

		// 3. Act & Assert
		mockMvc.perform(post("/users")
			   .contentType(MediaType.APPLICATION_JSON)
			   .content(objectMapper.writeValueAsString(userToCreate)))
			   
			   // Assert we get a 500 Internal Server Error
			   .andExpect(status().isInternalServerError()) 
			   
			   // Assert the generic, user-safe error message
			   .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."));
	}
}