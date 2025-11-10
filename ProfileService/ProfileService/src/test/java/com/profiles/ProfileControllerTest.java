package com.profiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.profiles.controllers.ProfileController;
import com.profiles.model.UserProfile;
import com.profiles.services.ProfileService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify; // Import verify
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ProfileController.class)
public class ProfileControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean
	private ProfileService profileService;
	
	@Test
	void whenPostProfile_thenReturns201Created() throws Exception {
		
		// 1. Arrange: Create our test payload
		UserProfile profileToCreate = new UserProfile(null,"ananthabharath01@gmail.com","My new bio");
		UserProfile savedProfile = new UserProfile("user-123","ananthabharath01@gmail.com","My new bio");
		String profileJson = objectMapper.writeValueAsString(profileToCreate);
		
		// 2. Mock the service's behavior
		when(profileService.createProfile(any(UserProfile.class))).thenReturn(savedProfile);
		
		// 3.Perform a POST to /profiles and assert the values
		mockMvc.perform(post("/profiles")
				.contentType(MediaType.APPLICATION_JSON)
				.content(profileJson))
		        .andExpect(status().isCreated())
		        .andExpect(jsonPath("$.userId").value("user-123"))
		        .andExpect(jsonPath("$.email").value("ananthabharath01@gmail.com"));
		 
		// 4. Verify: As requested, verify the service method was called
		verify(profileService).createProfile(any(UserProfile.class));
		
	}
}