package com.hotelbooking.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hotelbooking.base.BaseTest;
import com.hotelbooking.models.Booking;
import com.hotelbooking.utils.TestDataFactory;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class GetBookingTests extends BaseTest {

    @Test
    @DisplayName("GET /booking/{id} returns exactly what was created")
    void getById_returnsCreatedBooking() {
        Booking created = TestDataFactory.validBooking();
        int id = createBooking(created);

        Booking fetched = bookings.get(id).then().statusCode(200).extract().as(Booking.class);

        assertEquals(created, fetched);
    }

    @Test
    @DisplayName("GET /booking/{id} response contains all required fields")
    void getById_hasRequiredFields() {
        int id = createBooking(TestDataFactory.validBooking());

        bookings.get(id)
                .then()
                .statusCode(200)
                .body("firstname", notNullValue())
                .body("lastname", notNullValue())
                .body("totalprice", notNullValue())
                .body("depositpaid", notNullValue())
                .body("bookingdates.checkin", notNullValue())
                .body("bookingdates.checkout", notNullValue());
    }

    @Test
    @DisplayName("GET a booking id that does not exist returns 404")
    void nonexistentId_returns404() {
        bookings.get(999_999_999).then().statusCode(404);
    }

    @Test
    @DisplayName("GET with a non-numeric id is not found")
    void invalidId_isNotFound() {
        bookings.get("abc").then().statusCode(anyOf(is(400), is(404)));
    }

    @Test
    @DisplayName("GET /booking returns a list of objects that each have a bookingid")
    void list_returnsIds() {
        int id = createBooking(TestDataFactory.validBooking());

        Response response = bookings.list();
        response.then().statusCode(200);

        List<Integer> ids = response.jsonPath().getList("bookingid", Integer.class);
        assertThat(ids.size(), greaterThan(0));
        assertThat(ids, hasItem(id));
    }

    @Test
    @DisplayName("GET /booking?firstname=... filters by first name")
    void filterByFirstname() {
        Booking booking = TestDataFactory.validBooking();
        int id = createBooking(booking);

        List<Integer> ids = bookings.list(Map.of("firstname", booking.firstname()))
                .then().statusCode(200)
                .extract().jsonPath().getList("bookingid", Integer.class);

        assertThat(ids, hasItem(id));
    }

    @Test
    @DisplayName("GET /booking?lastname=... filters by last name")
    void filterByLastname() {
        Booking booking = TestDataFactory.validBooking();
        int id = createBooking(booking);

        List<Integer> ids = bookings.list(Map.of("lastname", booking.lastname()))
                .then().statusCode(200)
                .extract().jsonPath().getList("bookingid", Integer.class);

        assertThat(ids, hasItem(id));
    }

    @Test
    @DisplayName("Filter by a name nobody has returns an empty list")
    void filterByUnknownName_returnsEmptyList() {
        List<Integer> ids = bookings.list(Map.of("firstname", "NoSuchGuest" + System.nanoTime()))
                .then().statusCode(200)
                .extract().jsonPath().getList("bookingid", Integer.class);

        assertTrue(ids.isEmpty(), "expected no matches but got " + ids);
    }

    // Date filters are loosely documented, so this is exploratory (skipped by default).
    @Tag("exploratory")
    @Test
    @DisplayName("GET /booking?checkin=... returns the booking for an earlier date")
    void filterByCheckin() {
        Booking booking = TestDataFactory.validBooking();
        int id = createBooking(booking);
        String earlier = LocalDate.parse(booking.bookingdates().checkin()).minusDays(1).toString();

        List<Integer> ids = bookings.list(Map.of("checkin", earlier))
                .then().statusCode(200)
                .extract().jsonPath().getList("bookingid", Integer.class);

        assertThat(ids, hasItem(id));
    }
}
