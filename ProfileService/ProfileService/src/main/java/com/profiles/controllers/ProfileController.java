package com.profiles.controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.profiles.model.UserProfile;
import com.profiles.services.ProfileService;

@RestController
@RequestMapping("/profiles")
@CrossOrigin(origins = "*") // <--- ADD THIS. It tells the browser "I allow connections from anywhere"
public class ProfileController {

	@Autowired
	private ProfileService profileService;

	private static final Logger log = LoggerFactory.getLogger(ProfileController.class);
	
	@PostMapping
	public ResponseEntity<?> createProfile(@RequestBody UserProfile profile) {

        try {
            UserProfile savedProfile = profileService.createProfile(profile);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProfile);
        
        } catch (IllegalArgumentException ie) {
            log.error("Bad request for createProfile: {}", ie.getMessage());
            Map<String, String> errorResponse = Map.of(
                "status", "400",
                "error", "Bad Request",
                "message", ie.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        
        } catch (Exception e) {
            log.error("Internal Server Error during createProfile", e);
            Map<String, String> errorResponse = Map.of(
                "status", "500",
                "error", "Internal Server Error",
                "message", "An unexpected error occurred. Please try again later."
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
	}
	
	@GetMapping("/{userId}")
	public ResponseEntity<?>getProfileById(@PathVariable String userId){
		UserProfile profile = profileService.getProfileById(userId);
		if(profile != null) {
			return ResponseEntity.ok(profile);
		}else{
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("profile not found");
		}
	}
}