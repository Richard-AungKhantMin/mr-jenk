package com.example.mediaservice;

import com.example.mediaservice.service.MediaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;

@ExtendWith(value = MockitoExtension.class)
@DisplayName("MediaController Tests")
class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private MediaController mediaController;

    @BeforeEach
    void setUp() {
        // Use lenient() for stubbings not used in all tests
        lenient().when(mockFile.getOriginalFilename()).thenReturn("test-image.jpg");
        lenient().when(mockFile.getContentType()).thenReturn("image/jpeg");
        lenient().when(mockFile.getSize()).thenReturn(1024L);
    }

    @Test
    @DisplayName("Should upload image successfully")
    void testUploadImageSuccess() throws java.io.IOException {
        // Arrange
        com.example.mediaservice.model.Media media = new com.example.mediaservice.model.Media();
        media.setId("media-123");
        media.setFilename("test-image.jpg");
        media.setProductId("prod-123");
        media.setSize(1024L);

        // Mock SecurityContext to provide userId
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user-123");
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(mediaService.uploadImage(mockFile, "user-123", "prod-123"))
            .thenReturn(media);

        // Act
        ResponseEntity<?> response = mediaController.uploadImage(mockFile, "prod-123", null);

        // Assert
        assertNotNull(response);
        verify(mediaService, times(1)).uploadImage(mockFile, "user-123", "prod-123");

        // Clean up
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return 201 Created on successful upload")
    void testUploadImageReturnsCreated() throws java.io.IOException {
        // Arrange
        com.example.mediaservice.model.Media media = new com.example.mediaservice.model.Media();
        media.setId("media-123");
        media.setFilename("test-image.jpg");
        media.setProductId("prod-123");
        media.setSize(1024L);

        // Mock SecurityContext to provide userId
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user-123");
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(mediaService.uploadImage(any(MultipartFile.class), eq("user-123"), eq("prod-123")))
            .thenReturn(media);

        // Act
        ResponseEntity<?> response = mediaController.uploadImage(mockFile, "prod-123", null);

        // Assert
        assertNotNull(response);
        assertEquals(201, response.getStatusCode().value());
        verify(mediaService, times(1)).uploadImage(any(), eq("user-123"), eq("prod-123"));

        // Clean up
        SecurityContextHolder.clearContext();
    }
}
