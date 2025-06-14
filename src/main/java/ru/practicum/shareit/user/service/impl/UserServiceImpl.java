package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public List<UserDto> getAllUsers() {
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found."));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        if (userStorage.existsByEmail(user.getEmail())) {
            throw new ConflictException("User with email " + user.getEmail() + " already exists.");
        }
        return UserMapper.toUserDto(userStorage.save(user));
    }

    @Override
    public UserDto updateUser(long userId, UserDto userDto) {
        User existingUser = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found."));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            if (userStorage.existsByEmail(userDto.getEmail())) {
                throw new ConflictException("User with email " + userDto.getEmail() + " already exists.");
            }
            existingUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        return UserMapper.toUserDto(userStorage.update(existingUser));
    }

    @Override
    public void deleteUser(long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("User with id " + userId + " not found.");
        }
        userStorage.deleteById(userId);
    }
}