package com.hotelbooking.utils;

import com.hotelbooking.models.Booking;
import com.hotelbooking.models.BookingDates;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Builds controlled, unique test data. Names contain a random suffix so that
 * tests never collide with other people using the shared public API.
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static Booking validBooking() {
        return validBooking("Test" + shortId());
    }

    public static Booking validBooking(String firstname) {
        LocalDate checkin = LocalDate.now().plusDays(30);
        return new Booking(
                firstname,
                "Guest" + shortId(),
                150,
                true,
                new BookingDates(checkin.toString(), checkin.plusDays(3).toString()),
                "Breakfast");
    }

    /** A different, complete booking - used as the payload for PUT. */
    public static Booking updatedBooking() {
        LocalDate checkin = LocalDate.now().plusDays(60);
        return new Booking(
                "Updated" + shortId(),
                "Changed" + shortId(),
                275,
                false,
                new BookingDates(checkin.toString(), checkin.plusDays(5).toString()),
                "Late checkout");
    }

    public static Booking bookingWithPrice(int price) {
        Booking base = validBooking();
        return new Booking(base.firstname(), base.lastname(), price, base.depositpaid(),
                base.bookingdates(), base.additionalneeds());
    }

    public static Booking bookingWithDates(String checkin, String checkout) {
        Booking base = validBooking();
        return new Booking(base.firstname(), base.lastname(), base.totalprice(), base.depositpaid(),
                new BookingDates(checkin, checkout), base.additionalneeds());
    }

    /** Booking as a Map, so individual fields can be removed to build invalid payloads. */
    public static Map<String, Object> asMap(Booking b) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("firstname", b.firstname());
        map.put("lastname", b.lastname());
        map.put("totalprice", b.totalprice());
        map.put("depositpaid", b.depositpaid());
        Map<String, Object> dates = new LinkedHashMap<>();
        dates.put("checkin", b.bookingdates().checkin());
        dates.put("checkout", b.bookingdates().checkout());
        map.put("bookingdates", dates);
        map.put("additionalneeds", b.additionalneeds());
        return map;
    }

    /** A valid booking payload with one field removed. */
    public static Map<String, Object> bookingWithout(String field) {
        Map<String, Object> map = asMap(validBooking());
        map.remove(field);
        return map;
    }
}
