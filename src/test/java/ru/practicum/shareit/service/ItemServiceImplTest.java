package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.servece.impl.ItemServiceImpl;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Owner", "owner@example.com");
        item = new Item(1L, "Drill", "A powerful drill", true, user, null);
        itemDto = new ItemDto(1L, "Drill", "A powerful drill", true, null);
    }

    @Test
    void shouldCreateItem() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(itemStorage.save(any(Item.class))).thenReturn(item);

        ItemDto createdItem = itemService.createItem(1L, itemDto);

        assertNotNull(createdItem);
        assertEquals("Drill", createdItem.getName());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCreatingItemForNonExistentUser() {
        when(userStorage.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(99L, itemDto));
    }

    @Test
    void shouldUpdateItem() {
        ItemDto updates = new ItemDto(null, "Updated Drill", null, false, null);
        when(itemStorage.findById(1L)).thenReturn(Optional.of(item));
        when(itemStorage.update(any(Item.class))).thenAnswer(i -> i.getArgument(0));

        ItemDto updatedItem = itemService.updateItem(1L, 1L, updates);

        assertEquals("Updated Drill", updatedItem.getName());
        assertEquals(false, updatedItem.getAvailable());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingItemNotOwnedByUser() {
        long wrongUserId = 2L;
        ItemDto updates = new ItemDto(null, "Updated Drill", null, false, null);
        when(itemStorage.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.updateItem(wrongUserId, 1L, updates));
    }

    @Test
    void shouldGetItemsByOwner() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(itemStorage.findByOwnerId(1L)).thenReturn(List.of(item));

        List<ItemDto> items = itemService.getItemsByOwner(1L);

        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
    }

    @Test
    void shouldSearchItems() {
        when(itemStorage.search("drill")).thenReturn(List.of(item));

        List<ItemDto> foundItems = itemService.searchItems("drill");

        assertFalse(foundItems.isEmpty());
        assertEquals("Drill", foundItems.get(0).getName());
    }

    @Test
    void shouldReturnEmptyListForBlankSearch() {
        // Поиск по пустой строке не должен вызывать itemStorage.search,
        // а сразу вернуть пустой список (эта логика в InMemoryItemStorage).
        // Но для сервиса проверим, что он просто пробрасывает результат.
        when(itemStorage.search("")).thenReturn(List.of());

        List<ItemDto> foundItems = itemService.searchItems("");

        assertTrue(foundItems.isEmpty());
    }
}