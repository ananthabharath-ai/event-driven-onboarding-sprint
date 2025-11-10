package com.profiles;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.profiles.model.UserProfile;
import com.profiles.repositories.ProfileRepository;
import com.profiles.services.ProfileService;
import com.profiles.services.profileServiceImplementation;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

	@Mock
	private ProfileRepository profileRepository;
	
	private ProfileService profileService;
	
	@BeforeEach
	void setUp() {
		profileService = new profileServiceImplementation(profileRepository);	
	}
	
	@Test
	void whenCreateProfile_shouldSaveToRepository() {
		
		// 1. Arrange: Create our test payload
		UserProfile profileToSave = new UserProfile(null,"ananthabharath01@gmail.com","My new bio");
		UserProfile savedProfile = new UserProfile("user-123","ananthabharath01@gmail.com","My new bio");
		
        //2. Mock the repository's save method
		when(profileRepository.save(any(UserProfile.class))).thenReturn(savedProfile);
		
		//3. take the result of the service class createProfile
		UserProfile result = profileService.createProfile(profileToSave);
		
		// 4. Assert Verify the repository.save() method was called, as requested
		verify(profileRepository).save(any(UserProfile.class));
		
		// 5. Assert that the correct object was returned
		assertAll("Check UserProfile result",
				()->assertNotNull(result),
				()->assertEquals("user-123",result.getUserId()),
				()->assertEquals("ananthabharath01@gmail.com",result.getEmail())
				);
	}
}