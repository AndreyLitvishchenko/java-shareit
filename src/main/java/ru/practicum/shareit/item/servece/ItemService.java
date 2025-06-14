package ru.practicum.shareit.item.servece;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, long itemId, ItemDto itemDto);

    ItemDto getItemById(long itemId);

    List<ItemDto> getItemsByOwner(long userId);

    List<ItemDto> searchItems(String text);
}