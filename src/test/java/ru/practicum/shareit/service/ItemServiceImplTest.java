package ru.practicum.shareit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.servece.impl.ItemServiceImpl;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private User owner;
    private Item item;
    private ItemDto itemDto;
    private Comment comment;
    private Booking booking;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("User").email("user@example.com").build();
        owner = User.builder().id(2L).name("Owner").email("owner@example.com").build();
        item = Item.builder().id(1L).name("Drill").description("A powerful drill").available(true).owner(owner).build();
        itemDto = ItemDto.builder().id(1L).name("Drill").description("A powerful drill").available(true).build();
        comment = Comment.builder().id(1L).text("Great item!").item(item).author(user).created(LocalDateTime.now())
                .build();
        booking = Booking.builder().id(1L).item(item).booker(user).status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().minusDays(1)).end(LocalDateTime.now().minusHours(1)).build();
    }

    @Test
    void shouldCreateItem() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto createdItem = itemService.createItem(2L, itemDto);

        assertNotNull(createdItem);
        assertEquals("Drill", createdItem.getName());
    }

    @Test
    void shouldCreateItemWithRequest() {
        ItemRequest request = ItemRequest.builder().id(1L).description("Need a drill").build();
        itemDto.setRequestId(1L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto createdItem = itemService.createItem(2L, itemDto);

        assertNotNull(createdItem);
        assertEquals("Drill", createdItem.getName());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCreatingItemForNonExistentUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(99L, itemDto));
    }

    @Test
    void shouldUpdateItem() {
        ItemDto updates = ItemDto.builder().name("Updated Drill").available(false).build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArgument(0));

        ItemDto updatedItem = itemService.updateItem(2L, 1L, updates);

        assertEquals("Updated Drill", updatedItem.getName());
        assertEquals(false, updatedItem.getAvailable());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingItemNotOwnedByUser() {
        long wrongUserId = 1L;
        ItemDto updates = ItemDto.builder().name("Updated Drill").build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.updateItem(wrongUserId, 1L, updates));
    }

    @Test
    void shouldGetItemByIdForOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(1L)).thenReturn(List.of(comment));
        when(bookingRepository.findByItemIdAndStartBeforeOrderByStartDesc(
                eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED), any(Pageable.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findByItemIdAndStartAfterOrderByStartAsc(
                eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED), any(Pageable.class)))
                .thenReturn(List.of());

        ItemWithBookingDto result = itemService.getItemById(1L, 2L); // owner

        assertNotNull(result);
        assertEquals("Drill", result.getName());
        assertEquals(1, result.getComments().size());
        assertNotNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void shouldGetItemByIdForNonOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(1L)).thenReturn(List.of(comment));

        ItemWithBookingDto result = itemService.getItemById(1L, 1L);

        assertNotNull(result);
        assertEquals("Drill", result.getName());
        assertEquals(1, result.getComments().size());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void shouldGetItemsByOwner() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerIdOrderById(eq(2L), any(Pageable.class))).thenReturn(List.of(item));
        when(commentRepository.findByItemId(1L)).thenReturn(List.of(comment));
        when(bookingRepository.findByItemIdAndStartBeforeOrderByStartDesc(
                eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED), any(Pageable.class)))
                .thenReturn(List.of());
        when(bookingRepository.findByItemIdAndStartAfterOrderByStartAsc(
                eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED), any(Pageable.class)))
                .thenReturn(List.of());

        List<ItemWithBookingDto> items = itemService.getItemsByOwner(2L, 0, 10);

        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
        assertEquals(1, items.get(0).getComments().size());
    }

    @Test
    void shouldSearchItems() {
        when(itemRepository.findByText(eq("drill"), any(Pageable.class))).thenReturn(List.of(item));

        List<ItemDto> foundItems = itemService.searchItems("drill", 0, 10);

        assertFalse(foundItems.isEmpty());
        assertEquals("Drill", foundItems.get(0).getName());
    }

    @Test
    void shouldReturnEmptyListForBlankSearch() {
        List<ItemDto> foundItems = itemService.searchItems("", 0, 10);

        assertTrue(foundItems.isEmpty());
    }

    @Test
    void shouldAddComment() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItemIdAndEndBeforeAndStatus(
                eq(1L), eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto commentDto = CommentDto.builder().text("Great item!").build();
        CommentDto result = itemService.addComment(1L, 1L, commentDto);

        assertNotNull(result);
        assertEquals("Great item!", result.getText());
        assertEquals("User", result.getAuthorName());
    }

    @Test
    void shouldNotAddCommentIfUserDidNotBookItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItemIdAndEndBeforeAndStatus(
                eq(1L), eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of()); // empty list - no bookings

        CommentDto commentDto = CommentDto.builder().text("Great item!").build();

        assertThrows(ValidationException.class,
                () -> itemService.addComment(1L, 1L, commentDto));
    }
}
