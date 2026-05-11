package com.example.product.controller;

import com.example.product.exception.AccessDeniedException;
import com.example.product.exception.ResourceNotFoundException;
import com.example.product.model.AddImagesRequest;
import com.example.product.model.Product;
import com.example.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ProductController productController;

    private Product testProduct;
    private List<Product> productList;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId("prod-123");
        testProduct.setName("Test Product");
        testProduct.setDescription("A test product");
        testProduct.setPrice(99.99);
        testProduct.setQuantity(10);
        testProduct.setUserId("user-123");
        testProduct.setImageUrls(new ArrayList<>());
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());

        productList = List.of(testProduct);
    }

    @Test
    @DisplayName("Should get all products with default pagination")
    void testGetAllProductsWithDefaultPagination() {
        // Arrange
        when(productService.getAllProducts(0, 20)).thenReturn(productList);

        // Act
        ResponseEntity<List<Product>> response = productController.getAllProducts(0, 20);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Test Product", response.getBody().get(0).getName());

        verify(productService, times(1)).getAllProducts(0, 20);
    }

    @Test
    @DisplayName("Should get product by id successfully")
    void testGetProductByIdSuccess() {
        // Arrange
        when(productService.getProductById("prod-123")).thenReturn(Optional.of(testProduct));

        // Act
        ResponseEntity<Product> response = productController.getProductById("prod-123");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("prod-123", response.getBody().getId());
        assertEquals("Test Product", response.getBody().getName());

        verify(productService, times(1)).getProductById("prod-123");
    }

    @Test
    @DisplayName("Should return 404 when product not found by id")
    void testGetProductByIdNotFound() {
        // Arrange
        when(productService.getProductById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            productController.getProductById("nonexistent")
        );

        verify(productService, times(1)).getProductById("nonexistent");
    }

    @Test
    @DisplayName("Should create product successfully with SELLER role")
    void testCreateProductSuccess() {
        // Arrange
        when(productService.createProduct(any(Product.class), eq("user-123")))
            .thenReturn(testProduct);

        // Act
        ResponseEntity<Product> response = productController.createProduct(testProduct, "user-123");

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("prod-123", response.getBody().getId());

        verify(productService, times(1)).createProduct(testProduct, "user-123");
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProductSuccess() {
        // Arrange
        Product updatedProduct = new Product();
        updatedProduct.setId("prod-123");
        updatedProduct.setName("Updated Product");
        updatedProduct.setPrice(199.99);

        when(productService.updateProduct("prod-123", updatedProduct, "user-123"))
            .thenReturn(updatedProduct);

        // Act
        ResponseEntity<Product> response = productController.updateProduct("prod-123", updatedProduct, "user-123");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Product", response.getBody().getName());
        assertEquals(199.99, response.getBody().getPrice());

        verify(productService, times(1)).updateProduct("prod-123", updatedProduct, "user-123");
    }

    @Test
    @DisplayName("Should fail update when user is not authorized")
    void testUpdateProductUnauthorized() {
        // Arrange
        Product updatedProduct = new Product();
        updatedProduct.setId("prod-123");

        when(productService.updateProduct("prod-123", updatedProduct, "user-456"))
            .thenThrow(new AccessDeniedException("You are not authorized to update this product."));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> 
            productController.updateProduct("prod-123", updatedProduct, "user-456")
        );

        verify(productService, times(1)).updateProduct("prod-123", updatedProduct, "user-456");
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProductSuccess() {
        // Arrange
        doNothing().when(productService).deleteProduct("prod-123", "user-123");

        // Act
        ResponseEntity<Void> response = productController.deleteProduct("prod-123", "user-123");

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(productService, times(1)).deleteProduct("prod-123", "user-123");
    }

    @Test
    @DisplayName("Should fail delete when product not found")
    void testDeleteProductNotFound() {
        // Arrange
        doThrow(new ResourceNotFoundException("Product not found with id: prod-123"))
            .when(productService).deleteProduct("prod-123", "user-123");

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            productController.deleteProduct("prod-123", "user-123")
        );

        verify(productService, times(1)).deleteProduct("prod-123", "user-123");
    }

    @Test
    @DisplayName("Should add images to product successfully")
    void testAddImagesSuccess() {
        // Arrange
        AddImagesRequest request = new AddImagesRequest();
        request.setMediaIds(List.of("media-1", "media-2"));

        Product productWithImages = new Product();
        productWithImages.setId("prod-123");
        productWithImages.setImageUrls(List.of(
            "http://example.com/media-1.jpg",
            "http://example.com/media-2.jpg"
        ));

        when(productService.addImages("prod-123", List.of("media-1", "media-2"), "user-123"))
            .thenReturn(productWithImages);

        // Act
        ResponseEntity<Product> response = productController.addImages("prod-123", request, "user-123");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getImageUrls().size());

        verify(productService, times(1)).addImages("prod-123", List.of("media-1", "media-2"), "user-123");
    }

    @Test
    @DisplayName("Should fail adding images when unauthorized")
    void testAddImagesUnauthorized() {
        // Arrange
        AddImagesRequest request = new AddImagesRequest();
        request.setMediaIds(List.of("media-1"));

        when(productService.addImages("prod-123", List.of("media-1"), "user-456"))
            .thenThrow(new AccessDeniedException("You are not authorized to modify this product."));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> 
            productController.addImages("prod-123", request, "user-456")
        );
    }
}
