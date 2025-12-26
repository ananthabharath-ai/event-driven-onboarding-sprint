package com.user.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.model.User;
import com.user.services.UserService;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {	
  
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

	@PostMapping
	public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
    		User savedUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        
        } catch (IllegalArgumentException ie) {
            log.error("Bad request for createUser: {}", ie.getMessage());
            Map<String, String> errorResponse = Map.of(
                "status", "400",
                "error", "Bad Request",
                "message", ie.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        
        } catch (Exception e) {
            log.error("Internal Server Error during createUser", e);
            Map<String, String> errorResponse = Map.of(
                "status", "500",
                "error", "Internal Server Error",
                "message", "An unexpected error occurred. Please try again later."
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
	}
}