package com.podcast.voice.service;

import com.podcast.voice.entity.UserEntity;
import com.podcast.voice.repository.UserRepository;
import com.podcast.voice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        // Given
        UserEntity user = new UserEntity("testuser", "password123", "test@example.com", "USER");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        // When
        UserEntity result = userService.register(user);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPasswordHash());
        assertTrue(result.getActive());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void testAuthenticate_Success() {
        // Given
        UserEntity user = new UserEntity("testuser", "encodedPassword", "test@example.com", "USER");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // When
        boolean result = userService.authenticate("test@example.com", "password123");

        // Then
        assertTrue(result);
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    @Test
    void testAuthenticate_Failure_WrongPassword() {
        // Given
        UserEntity user = new UserEntity("testuser", "encodedPassword", "test@example.com", "USER");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // When
        boolean result = userService.authenticate("test@example.com", "wrongpassword");

        // Then
        assertFalse(result);
    }

    @Test
    void testFindByEmail_Success() {
        // Given
        UserEntity user = new UserEntity("testuser", "encodedPassword", "test@example.com", "USER");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When
        UserEntity result = userService.findByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testFindByEmail_NotFound() {
        // Given
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When
        UserEntity result = userService.findByEmail("notfound@example.com");

        // Then
        assertNull(result);
    }
}