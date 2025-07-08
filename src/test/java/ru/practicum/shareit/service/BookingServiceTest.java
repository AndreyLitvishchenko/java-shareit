package ru.practicum.shareit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.impl.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John Doe").email("john@example.com").build();
        owner = User.builder().id(2L).name("Jane Doe").email("jane@example.com").build();
        item = Item.builder().id(1L).name("Drill").description("Powerful drill").available(true).owner(owner).build();

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        booking = Booking.builder().id(1L).start(start).end(end).item(item).booker(user).status(BookingStatus.WAITING)
                .build();
        bookingDto = BookingDto.builder().itemId(1L).start(start).end(end).build();
    }

    @Test
    void shouldCreateBooking() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.createBooking(1L, bookingDto);

        assertNotNull(result);
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldNotCreateBookingWhenItemNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(1L, bookingDto));
    }

    @Test
    void shouldNotCreateBookingWhenOwnerTriesToBook() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(2L, bookingDto));
    }

    @Test
    void shouldNotCreateBookingWithInvalidDates() {
        BookingDto invalidDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1)) // end before start
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(1L, invalidDto));
    }

    @Test
    void shouldUpdateBookingStatus() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        BookingResponseDto result = bookingService.updateBookingStatus(2L, 1L, true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void shouldRejectBookingStatus() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        BookingResponseDto result = bookingService.updateBookingStatus(2L, 1L, false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    void shouldNotUpdateBookingStatusWhenNotOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class,
                () -> bookingService.updateBookingStatus(1L, 1L, true)); // user is not owner
    }

    @Test
    void shouldGetBookingById() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBookingById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldGetBookingByIdForOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBookingById(2L, 1L); // owner

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldNotGetBookingByIdForOtherUser() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(99L, 1L)); // other user
    }

    @Test
    void shouldGetUserBookings() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdOrderByStartDesc(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByUser(1L, "ALL", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetOwnerBookings() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_OwnerIdOrderByStartDesc(eq(2L), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(2L, "ALL", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetUserBookingsWithWaitingState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(eq(1L), eq(BookingStatus.WAITING),
                any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByUser(1L, "WAITING", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowExceptionForInvalidState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class,
                () -> bookingService.getBookingsByUser(1L, "INVALID_STATE", 0, 10));
    }
}
