package com.hotelbooking.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response of POST /booking: the generated id plus the stored booking. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingResponse(int bookingid, Booking booking) {
}
