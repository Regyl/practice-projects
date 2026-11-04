package com.github.regyl.hotelbooking.service;

import com.github.regyl.annotation.DefaultUnitTest;
import com.github.regyl.hotelbooking.exception.OverlappingPeriodException;
import com.github.regyl.hotelbooking.model.BookingModel;
import com.github.regyl.hotelbooking.model.GuestModel;
import com.github.regyl.hotelbooking.model.RoomModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Slf4j
@DefaultUnitTest
class HotelBookingServiceImplTest {

    private static final AtomicLong GUEST_SEQUENCE = new AtomicLong(0);
    private static final AtomicLong ROOM_SEQUENCE = new AtomicLong(0);

    private HotelBookingServiceImpl service;

    private final Map<Long, GuestModel>  guests = new HashMap<>();
    private final Map<Long, RoomModel> rooms = new HashMap<>();
    private final ConcurrentMap<Long, BookingModel> bookings = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        service = new HotelBookingServiceImpl(guests, rooms, bookings);
    }


    @Test
    void book_should_reserve_the_room() {
        //given
        Long guestId = GUEST_SEQUENCE.incrementAndGet();
        GuestModel guest = new GuestModel(guestId);
        guests.put(guestId, guest);
        Long roomId = ROOM_SEQUENCE.incrementAndGet();
        RoomModel room = new RoomModel(roomId);
        rooms.put(roomId, room);
        LocalDate dateStart = LocalDate.of(2026, 1, 1);
        LocalDate dateEnd = LocalDate.of(2026, 1, 5);

        //when
        //FIXME how to store dates in international companies
        BookingModel booking = service.book(guestId, roomId, dateStart, dateEnd);

        //then
        assertThat(booking).isNotNull();
        assertThat(booking.getGuestId()).isEqualTo(guestId);
        assertThat(booking.getRoomId()).isEqualTo(roomId);
        assertThat(booking.getDateStart()).isEqualTo(dateStart);
        assertThat(booking.getDateEnd()).isEqualTo(dateEnd);
    }

    @Test
    void book_should_throw_exception_on_null_guest_id() {
        //given
        Long roomId = ROOM_SEQUENCE.incrementAndGet();
        RoomModel room = new RoomModel(roomId);
        rooms.put(roomId, room);
        LocalDate dateStart = LocalDate.of(2026, 1, 1);
        LocalDate dateEnd = LocalDate.of(2026, 1, 5);

        //then
        assertThatCode(() -> service.book(null, roomId, dateStart, dateEnd)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void book_should_throw_exception_on_invalid_guest_id() {
        //given
        Long guestId = GUEST_SEQUENCE.incrementAndGet();
        Long roomId = ROOM_SEQUENCE.incrementAndGet();
        RoomModel room = new RoomModel(roomId);
        rooms.put(roomId, room);
        LocalDate dateStart = LocalDate.of(2026, 1, 1);
        LocalDate dateEnd = LocalDate.of(2026, 1, 5);

        //then
        assertThatCode(() -> service.book(guestId, roomId, dateStart, dateEnd)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void book_should_throw_exception_if_not_free() {
        //when
        Long guestId = GUEST_SEQUENCE.incrementAndGet();
        GuestModel guest = new GuestModel(guestId);
        guests.put(guestId, guest);
        Long roomId = ROOM_SEQUENCE.incrementAndGet();
        RoomModel room = new RoomModel(roomId);
        rooms.put(roomId, room);
        LocalDate dateStart = LocalDate.of(2026, 1, 1);
        LocalDate dateEnd = LocalDate.of(2026, 1, 5);
        service.book(guestId, roomId, dateStart, dateEnd);
        //second guest
        Long guestIdSecond = GUEST_SEQUENCE.incrementAndGet();
        GuestModel guestSecond = new GuestModel(guestIdSecond);
        guests.put(guestIdSecond, guestSecond);

        assertThatCode(() -> service.book(guestIdSecond, roomId, dateEnd.minusDays(1L), dateEnd.plusDays(1L)))
                .isInstanceOf(OverlappingPeriodException.class).hasMessage("Room is already reserved for these days");
    }

    @Test
    void room_should_be_allowed_to_book_for_the_same_date() {
        //when
        Long guestId = GUEST_SEQUENCE.incrementAndGet();
        GuestModel guest = new GuestModel(guestId);
        guests.put(guestId, guest);
        Long roomId = ROOM_SEQUENCE.incrementAndGet();
        RoomModel room = new RoomModel(roomId);
        rooms.put(roomId, room);
        LocalDate dateStart = LocalDate.of(2026, 1, 1);
        LocalDate dateEnd = LocalDate.of(2026, 1, 5);
        service.book(guestId, roomId, dateStart, dateEnd);
        //second guest
        Long guestIdSecond = GUEST_SEQUENCE.incrementAndGet();
        GuestModel guestSecond = new GuestModel(guestIdSecond);
        guests.put(guestIdSecond, guestSecond);

        assertThatCode(() -> service.book(guestIdSecond, roomId, dateEnd, dateEnd.plusDays(1L))).doesNotThrowAnyException();
    }

    //FIXME test that startDate isAfter now

    @Test
    void test_race_condition_during_booking() {
        //given
        Long guestId = GUEST_SEQUENCE.incrementAndGet();
        GuestModel guest = new GuestModel(guestId);
        guests.put(guestId, guest);
        Long roomId = ROOM_SEQUENCE.incrementAndGet();
        RoomModel room = new RoomModel(roomId);
        rooms.put(roomId, room);

        //when
        int iterations = 10_000;
        LocalDate initialDate = LocalDate.of(2026, 1, 1);
        List<CompletableFuture<BookingModel>> futures = new ArrayList<>();
        try (ExecutorService executorService =  Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= iterations; i++) {
                LocalDate dateStart = initialDate;
                LocalDate dateEnd = dateStart.plusDays(1);
                CompletableFuture<BookingModel> futureFirst = CompletableFuture.supplyAsync(() -> {
                    try {
                        return service.book(guestId, roomId, dateStart, dateEnd);
                    } catch (OverlappingPeriodException e) {
                        return null;
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        return null;
                    }
                },  executorService);
                CompletableFuture<BookingModel> futureSecond = CompletableFuture.supplyAsync(() -> {
                    try {
                        return service.book(guestId, roomId, dateStart, dateEnd);
                    } catch (OverlappingPeriodException e) {
                        return null;
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        return null;
                    }
                }, executorService);
                futures.add(futureFirst);
                futures.add(futureSecond);
                initialDate = initialDate.plusDays(1);
            }
        }

        //then
        List<BookingModel> res = futures.stream().map(item -> {
            try {
                return item.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.getMessage());
                return null;
            }
        }).filter(Objects::nonNull).toList();
        assertThat(res.size()).isEqualTo(iterations);
        assertThat(bookings.size()).isEqualTo(iterations);
    }
}