package com.profiles;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.profile.config.DynamoDbTestContainerConfig;
import com.profiles.model.UserProfile;
import com.profiles.repositories.ProfileRepository;

/**
 * This test will fail with UnsatisfiedDependencyException.
 * Spring cannot create the ProfileRepository bean because
 * UserProfile is not a valid @DynamoDbBean.
 */
@SpringBootTest
@Import(DynamoDbTestContainerConfig.class) // Import our container config
public class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    // We have to create the table before each test
    @BeforeEach
    void setupTable() {
        // This line will fail because profileRepository is null
        profileRepository.createTable();
    }

    @Test
    void whenSaveProfile_thenFindById_ReturnsSameProfile() {
        // Arrange
        UserProfile profile = new UserProfile("u123", "test@example.com", "My test bio");

        // Act
        profileRepository.save(profile);
        UserProfile found = profileRepository.findById("u123");

        // Assert
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("test@example.com");
        assertThat(found.getBio()).isEqualTo("My test bio");
    }
}