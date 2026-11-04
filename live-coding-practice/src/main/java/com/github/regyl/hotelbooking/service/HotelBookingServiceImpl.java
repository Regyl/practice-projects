package com.github.regyl.hotelbooking.service;

import com.github.regyl.hotelbooking.exception.OverlappingPeriodException;
import com.github.regyl.hotelbooking.model.BookingModel;
import com.github.regyl.hotelbooking.model.GuestModel;
import com.github.regyl.hotelbooking.model.RoomModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Getter
@RequiredArgsConstructor
public class HotelBookingServiceImpl {

    private static final AtomicLong BOOKING_SEQUENCE = new AtomicLong(0);
    private final Map<Long, GuestModel> guests;
    private final Map<Long, RoomModel> rooms;
    private final ConcurrentMap<Long, BookingModel> bookings; //important if we inserting multiple rooms at once

    //modify params to a single DTO
    public BookingModel book(Long guestId, Long roomId, LocalDate dateStart, LocalDate dateEnd) {
        if (guestId == null || !guests.containsKey(guestId)) {
            throw new IllegalArgumentException("Guest id not found");
        }
        if (roomId == null || !rooms.containsKey(roomId)) {
            throw new IllegalArgumentException("Room id not found");
        }
        RoomModel room = rooms.get(roomId);
        if (dateStart == null || dateEnd == null || dateEnd.isBefore(dateStart)) {
            throw new  IllegalArgumentException("Wrong booking dates");
        }

        room.getLock().lock();
        try {
            return book0(guestId, roomId, dateStart, dateEnd);
        } finally {
            room.getLock().unlock();
        }
    }

    private BookingModel book0(Long guestId, Long roomId, LocalDate dateStart, LocalDate dateEnd) {
        boolean alreadyReserved = bookings.entrySet().stream()
                .filter(booking -> booking.getValue().getRoomId().equals(roomId))
                .anyMatch(booking -> booking.getValue().getDateEnd().isAfter(dateStart)
                        &&  booking.getValue().getDateStart().isBefore(dateEnd));
        if (alreadyReserved) {
            throw new OverlappingPeriodException("Room is already reserved for these days"); //improve exception
        }

        Long bookingId = BOOKING_SEQUENCE.incrementAndGet();
        BookingModel booking = new BookingModel(bookingId, guestId, roomId, dateStart, dateEnd);
        bookings.put(bookingId, booking);
        return booking;
    }
}
