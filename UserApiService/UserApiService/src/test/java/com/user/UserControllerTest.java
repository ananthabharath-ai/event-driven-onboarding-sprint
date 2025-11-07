package com.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
		
		// 1. Create our test user object
		User user = new User(Long.parseLong("1"), "bharath","bharath01@gmail.com");
		
		//2. Convert the user object to Json String
		String userJson = objectMapper.writeValueAsString(user);
		
		//3. Assert: Perform the POST request and check the response
		mockMvc.perform(post("/users") // Perform POST to /users
                .contentType(MediaType.APPLICATION_JSON) // Set the content type header
                .content(userJson)) // Set the request body
                
		//4. This is the assertion that will Fail. We expect 201 (Created), but will get 404 (Not Found)
		//because the endpoint doesn't exist.
                .andExpect(status().isCreated());
	}
}