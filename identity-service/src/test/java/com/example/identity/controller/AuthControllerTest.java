package com.example.identity.controller;

import com.example.identity.model.Role;
import com.example.identity.model.User;
import com.example.identity.repository.UserRepository;
import com.example.identity.service.JwtService;
import com.example.identity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("AuthController Tests")
class AuthControllerTest 

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @Autowired
    private AuthController authController;

    private User testUser;
    private Map<String, String> loginRequest;
    private Map<String, String> registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setRole(Role.CLIENT);

        loginRequest = Map.of(
            "email", "john@example.com",
            "password", "password123"
        );

        registerRequest = Map.of(
            "name", "John Doe",
            "email", "john@example.com",
            "password", "password123",
            "role", "CLIENT"
        );
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void testRegisterSuccess() {
        // Arrange
        when(userService.register(
            eq("John Doe"),
            eq("john@example.com"),
            eq("password123"),
            eq(Role.CLIENT)
        )).thenReturn(testUser);

        // Act
        ResponseEntity<?> response = authController.register(registerRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("user-123", body.get("id"));
        assertEquals("John Doe", body.get("name"));
        assertEquals("john@example.com", body.get("email"));
        assertEquals("CLIENT", body.get("role"));
        assertEquals("Registration successful. Please login.", body.get("message"));
        
        verify(userService, times(1)).register(
            "John Doe", "john@example.com", "password123", Role.CLIENT
        );
    }

    @Test
    @DisplayName("Should fail registration with invalid role")
    void testRegisterInvalidRole() {
        // Arrange
        Map<String, String> invalidRequest = Map.of(
            "name", "John Doe",
            "email", "john@example.com",
            "password", "password123",
            "role", "INVALID_ROLE"
        );

        when(userService.register(anyString(), anyString(), anyString(), any()))
            .thenThrow(new IllegalArgumentException("Invalid role"));

        // Act
        ResponseEntity<?> response = authController.register(invalidRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("error"));
    }

    @Test
    @DisplayName("Should fail registration if email already exists")
    void testRegisterEmailAlreadyExists() {
        // Arrange
        when(userService.register(anyString(), anyString(), anyString(), any()))
            .thenThrow(new RuntimeException("Email already exists"));

        // Act
        ResponseEntity<?> response = authController.register(registerRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        // Arrange
        String token = "jwt-token-123";
        when(userService.authenticate("john@example.com", "password123"))
            .thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(token);

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("user-123", body.get("id"));
        assertEquals("John Doe", body.get("name"));
        assertEquals("john@example.com", body.get("email"));
        assertEquals("CLIENT", body.get("role"));
        assertEquals(token, body.get("token"));

        verify(userService, times(1)).authenticate("john@example.com", "password123");
        verify(jwtService, times(1)).generateToken(testUser);
    }

    @Test
    @DisplayName("Should fail login with invalid credentials")
    void testLoginInvalidCredentials() {
        // Arrange
        when(userService.authenticate("john@example.com", "wrongpassword"))
            .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = authController.login(Map.of(
            "email", "john@example.com",
            "password", "wrongpassword"
        ));

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Invalid credentials", body.get("error"));

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should handle login service exception")
    void testLoginServiceException() {
        // Arrange
        when(userService.authenticate(anyString(), anyString()))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue(body.get("error").toString().contains("Login failed"));
    }

    @Test
    @DisplayName("Should get user profile for authenticated user")
    @WithMockUser(username = "john@example.com")
    void testGetProfileSuccess() {
        // Arrange
        when(authentication.getName()).thenReturn("john@example.com");
        when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authController.getProfile(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("user-123", body.get("id"));
        assertEquals("John Doe", body.get("name"));
        assertEquals("john@example.com", body.get("email"));
        assertEquals("CLIENT", body.get("role"));

        verify(userService, times(1)).findByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should return 404 when user profile not found")
    @WithMockUser(username = "nonexistent@example.com")
    void testGetProfileNotFound() {
        // Arrange
        when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = authController.getProfile(authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
