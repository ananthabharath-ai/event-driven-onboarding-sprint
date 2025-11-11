package com.profiles.services;

import org.springframework.stereotype.Service;

import com.profiles.model.UserProfile;
import com.profiles.repositories.ProfileRepository;

@Service
public class profileServiceImplementation implements ProfileService {

	private final ProfileRepository profileRepository;

    // Constructor injection
    public profileServiceImplementation(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }
	
	@Override
	public UserProfile createProfile(UserProfile user) {
		return profileRepository.save(user);
	}
}