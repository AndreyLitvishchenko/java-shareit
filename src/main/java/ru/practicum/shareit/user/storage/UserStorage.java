package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    List<User> findAll();

    Optional<User> findById(long userId);

    User save(User user);

    User update(User user);

    void deleteById(long userId);

    boolean existsByEmail(String email);
}