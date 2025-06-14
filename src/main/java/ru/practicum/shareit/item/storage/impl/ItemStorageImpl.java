package ru.practicum.shareit.item.storage.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ItemStorageImpl implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private long currentId = 1;

    @Override
    public Item save(Item item) {
        item.setId(currentId++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> findByOwnerId(long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId() == ownerId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        String lowerCaseText = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(lowerCaseText) ||
                        item.getDescription().toLowerCase().contains(lowerCaseText))
                .collect(Collectors.toList());
    }
}