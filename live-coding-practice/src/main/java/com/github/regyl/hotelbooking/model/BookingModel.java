package com.github.regyl.hotelbooking.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class BookingModel {

    private Long id;
    private Long guestId;
    private Long roomId;
    private LocalDate dateStart;
    private LocalDate dateEnd;
}
