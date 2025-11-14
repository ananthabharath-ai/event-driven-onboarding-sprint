package com.profiles.services;

import com.profiles.model.UserProfile;

public interface ProfileService {
	UserProfile createProfile(UserProfile user);
	UserProfile getProfileById(String userId);
}