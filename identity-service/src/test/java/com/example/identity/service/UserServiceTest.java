package com.example.identity.service;

import com.example.identity.model.Role;
import com.example.identity.model.User;
import com.example.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("UserService Tests")
class UserServiceTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private UserService userService;

    private User testUser;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);

        testUser = new User();
        testUser.setId("user-123");
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(Role.CLIENT);
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void testRegisterSuccess() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.register("John Doe", "john@example.com", "password123", Role.CLIENT);

        // Assert
        assertNotNull(result);
        assertEquals("user-123", result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals(Role.CLIENT, result.getRole());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
        
        verify(kafkaTemplate, times(1)).send("user-registered", testUser.getId());
    }

    @Test
    @DisplayName("Should fail registration if email already exists")
    void testRegisterEmailAlreadyExists() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            userService.register("John Doe", "john@example.com", "password123", Role.CLIENT)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    @DisplayName("Should authenticate user with valid credentials")
    void testAuthenticateSuccess() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.authenticate("john@example.com", "password123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        verify(userRepository, times(1)).findByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should fail authentication with invalid password")
    void testAuthenticateInvalidPassword() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.authenticate("john@example.com", "wrongpassword");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should fail authentication with non-existent user")
    void testAuthenticateUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.authenticate("nonexistent@example.com", "password123");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmailSuccess() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByEmail("john@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void testFindByEmailNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void testUpdateProfileSuccess() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        User updatedUser = new User();
        updatedUser.setId("user-123");
        updatedUser.setName("Jane Doe");
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        Optional<User> result = userService.updateProfile("john@example.com", "Jane Doe");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Jane Doe", result.get().getName());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should fail update profile with old password mismatch")
    void testUpdateProfileWithPasswordMismatch() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            userService.updateProfileWithPassword("john@example.com", "Jane Doe", "john@example.com", 
                                                   "wrongoldpassword", "newpassword123")
        );

        assertEquals("Old password is incorrect", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update password with correct old password")
    void testUpdateProfilePasswordSuccess() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        User updatedUser = new User();
        updatedUser.setId("user-123");
        updatedUser.setName("John Doe");
        updatedUser.setPassword(passwordEncoder.encode("newpassword123"));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        Optional<User> result = userService.updateProfileWithPassword("john@example.com", "John Doe", 
                                                                        "john@example.com", "password123", "newpassword123");

        // Assert
        assertTrue(result.isPresent());
        verify(userRepository, times(1)).save(any(User.class));
    }
}
