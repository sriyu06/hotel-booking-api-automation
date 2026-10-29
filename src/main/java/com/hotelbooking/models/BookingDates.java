package com.hotelbooking.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Dates are ISO strings (yyyy-MM-dd), exactly as the API sends them. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingDates(String checkin, String checkout) {
}
