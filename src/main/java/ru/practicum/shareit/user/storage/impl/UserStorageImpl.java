package ru.practicum.shareit.user.storage.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserStorageImpl implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public User save(User user) {
        user.setId(currentId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteById(long userId) {
        users.remove(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.values().stream().anyMatch(user -> user.getEmail().equals(email));
    }
}
