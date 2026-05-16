package com.example.identity.service;

import com.example.identity.model.Role;
import com.example.identity.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest 

    private JwtService jwtService;
    private final String jwtSecret = "this-is-a-very-long-secret-key-for-jwt-signing-at-least-256-bits";
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtSecret);
        
        testUser = new User();
        testUser.setId("user-123");
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setRole(Role.CLIENT);
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void testGenerateTokenSuccess() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    @DisplayName("Should contain correct claims in generated token")
    void testGeneratedTokenContainsCorrectClaims() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        Key key = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());

        // Act
        @SuppressWarnings("deprecation")
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        // Assert
        assertEquals("john@example.com", claims.getSubject());
        assertEquals("user-123", claims.get("userId"));
        assertEquals("CLIENT", claims.get("role"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("Should have expiration of 1 day")
    void testTokenExpirationTime() {
        // Arrange
        long beforeGeneration = System.currentTimeMillis();
        String token = jwtService.generateToken(testUser);
        long afterGeneration = System.currentTimeMillis();
        Key key = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());

        // Act
        @SuppressWarnings("deprecation")
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        // Assert
        long expirationTime = claims.getExpiration().getTime();
        long expectedExpiration = afterGeneration + 86400000; // 1 day
        assertTrue(expirationTime > beforeGeneration + 86400000 - 1000); // Allow 1s tolerance
        assertTrue(expirationTime <= expectedExpiration + 1000);
    }

    @Test
    @DisplayName("Should reject empty user data")
    void testGenerateTokenWithNullUser() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> jwtService.generateToken(null));
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerateDifferentTokensForDifferentUsers() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId("user-456");
        anotherUser.setEmail("jane@example.com");
        anotherUser.setRole(Role.SELLER);

        // Act
        String token1 = jwtService.generateToken(testUser);
        String token2 = jwtService.generateToken(anotherUser);

        // Assert
        assertNotEquals(token1, token2);

        Key key = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        
        @SuppressWarnings("deprecation")
        Claims claims1 = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token1)
            .getBody();
        
        @SuppressWarnings("deprecation")
        Claims claims2 = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token2)
            .getBody();

        assertEquals("user-123", claims1.get("userId"));
        assertEquals("user-456", claims2.get("userId"));
    }

    @Test
    @DisplayName("Should reject malformed token")
    void testMalformedToken() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            Key key = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());
            @SuppressWarnings("deprecation")
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws("malformed.token.here")
                .getBody();
        });
    }

    @Test
    @DisplayName("Should reject token with wrong signature")
    void testTokenWithWrongSignature() {
        // Arrange
        String wrongSecret = "this-is-a-different-secret-key-for-jwt-signing-256-bits";
        String token = jwtService.generateToken(testUser);

        // Act & Assert
        Key wrongKey = new SecretKeySpec(wrongSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        assertThrows(Exception.class, () -> {
            @SuppressWarnings("deprecation")
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(wrongKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        });
    }

    @Test
    @DisplayName("Should extract username (email) from token")
    void testExtractUsernameFromToken() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        Key key = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());

        // Act
        @SuppressWarnings("deprecation")
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        // Assert
        assertEquals("john@example.com", claims.getSubject());
    }

    @Test
    @DisplayName("Token should be cryptographically signed")
    void testTokenIsSignedProperly() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act & Assert
        // If token is not properly signed, this will throw an exception
        Key key = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        assertDoesNotThrow(() -> {
            @SuppressWarnings("deprecation")
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
            assertNotNull(claims);
        });
    }

    @Test
    @DisplayName("Should handle different roles correctly")
    void testTokenWithDifferentRoles() {
        // Arrange
        testUser.setRole(Role.SELLER);
        String token = jwtService.generateToken(testUser);
        Key key = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());

        // Act
        @SuppressWarnings("deprecation")
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        // Assert
        assertEquals("SELLER", claims.get("role"));
    }
}
