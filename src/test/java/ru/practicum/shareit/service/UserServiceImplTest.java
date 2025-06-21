package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.impl.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "John Doe", "john.doe@example.com");
        userDto = new UserDto(1L, "John Doe", "john.doe@example.com");
    }

    @Test
    void shouldCreateUser() {
        when(userStorage.existsByEmail(anyString())).thenReturn(false);
        when(userStorage.save(any(User.class))).thenReturn(user);

        UserDto createdUser = userService.createUser(userDto);

        assertNotNull(createdUser);
        assertEquals(userDto.getName(), createdUser.getName());
        verify(userStorage).save(any(User.class));
    }

    @Test
    void shouldThrowConflictExceptionWhenEmailExists() {
        when(userStorage.existsByEmail(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(userDto));
        verify(userStorage, never()).save(any(User.class));
    }

    @Test
    void shouldGetUserById() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));

        UserDto foundUser = userService.getUserById(1L);

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userStorage.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void shouldUpdateUser() {
        UserDto updates = new UserDto(0, "Jane Doe", "jane.doe@example.com");
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.existsByEmail("jane.doe@example.com")).thenReturn(false);
        when(userStorage.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto updatedUser = userService.updateUser(1L, updates);

        assertEquals("Jane Doe", updatedUser.getName());
        assertEquals("jane.doe@example.com", updatedUser.getEmail());
    }

    @Test
    void shouldDeleteUser() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userStorage).deleteById(1L);

        userService.deleteUser(1L);

        verify(userStorage, times(1)).deleteById(1L);
    }

    @Test
    void shouldGetAllUsers() {
        when(userStorage.findAll()).thenReturn(List.of(user));

        List<UserDto> users = userService.getAllUsers();

        assertEquals(1, users.size());
        assertEquals(user.getName(), users.get(0).getName());
    }
}