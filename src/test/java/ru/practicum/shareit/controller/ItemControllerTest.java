package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.servece.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private final long userId = 1L;

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto(1L, "Drill", "Powerful drill", true, null);
    }

    @Test
    void shouldCreateItem() throws Exception {
        when(itemService.createItem(anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void shouldNotCreateItemIfUserNotFound() throws Exception {
        when(itemService.createItem(anyLong(), any(ItemDto.class)))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateItem() throws Exception {
        ItemDto updatedDto = new ItemDto(1L, "Updated Drill", "More powerful", false, null);
        when(itemService.updateItem(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(updatedDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Drill"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void shouldGetItemById() throws Exception {
        when(itemService.getItemById(1L)).thenReturn(itemDto);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldGetItemsByOwner() throws Exception {
        when(itemService.getItemsByOwner(userId)).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void shouldSearchItems() throws Exception {
        when(itemService.searchItems(anyString())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }
}