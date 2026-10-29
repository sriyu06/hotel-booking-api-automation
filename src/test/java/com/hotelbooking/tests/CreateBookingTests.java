package com.hotelbooking.tests;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hotelbooking.base.BaseTest;
import com.hotelbooking.models.Booking;
import com.hotelbooking.models.BookingResponse;
import com.hotelbooking.utils.TestDataFactory;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CreateBookingTests extends BaseTest {

    @Test
    @DisplayName("Valid booking returns 200, a booking id and echoes every field")
    void validBooking_isCreated() {
        Booking request = TestDataFactory.validBooking();

        Response response = bookings.create(request);
        response.then().statusCode(200).body("bookingid", greaterThan(0));

        BookingResponse body = response.as(BookingResponse.class);
        track(body.bookingid()); // BaseTest deletes it after the test

        assertAll("response echoes the request",
                () -> assertEquals(request.firstname(), body.booking().firstname()),
                () -> assertEquals(request.lastname(), body.booking().lastname()),
                () -> assertEquals(request.totalprice(), body.booking().totalprice()),
                () -> assertEquals(request.depositpaid(), body.booking().depositpaid()),
                () -> assertEquals(request.bookingdates(), body.booking().bookingdates()),
                () -> assertEquals(request.additionalneeds(), body.booking().additionalneeds()));
    }

    // Restful-Booker is known to answer with 500 for incomplete payloads; 400 would be the
    // "correct" REST answer. We accept either so the test documents intent, not a server bug.
    @ParameterizedTest(name = "missing field: {0}")
    @DisplayName("Booking with a missing required field is rejected")
    @ValueSource(strings = {"firstname", "lastname", "totalprice", "depositpaid", "bookingdates"})
    void missingField_isRejected(String field) {
        bookings.create(TestDataFactory.bookingWithout(field))
                .then()
                .statusCode(rejected());
    }

    @Test
    @DisplayName("Malformed JSON is rejected")
    void malformedJson_isRejected() {
        bookings.create("{ \"firstname\": \"Broken\", ")
                .then()
                .statusCode(rejected());
    }

    @Test
    @DisplayName("Wrong data type (text in totalprice) is rejected")
    void wrongType_isRejected() {
        var payload = TestDataFactory.asMap(TestDataFactory.validBooking());
        payload.put("totalprice", "not-a-number");

        bookings.create(payload)
                .then()
                .statusCode(rejected());
    }

    // ---- Boundary tests -------------------------------------------------------------
    // The public docs do not define limits for price or dates, so these record how the
    // service actually behaves. They are tagged "exploratory" and skipped by default.
    // Run them with:  mvn test -DexcludedGroups=none

    @Tag("exploratory")
    @ParameterizedTest(name = "price boundary: {0}")
    @DisplayName("Boundary: totalprice values are accepted and echoed")
    @ValueSource(ints = {0, 1, Integer.MAX_VALUE})
    void priceBoundaries(int price) {
        Booking request = TestDataFactory.bookingWithPrice(price);
        int id = createBooking(request);
        bookings.get(id).then().statusCode(200).body("totalprice", equalTo(price));
    }

    @Tag("exploratory")
    @Test
    @DisplayName("Boundary: checkout before checkin - records whether the API validates dates")
    void checkoutBeforeCheckin_behaviourIsRecorded() {
        Response response = bookings.create(TestDataFactory.bookingWithDates("2027-06-10", "2027-06-01"));
        if (response.statusCode() == 200) {
            track(response.jsonPath().getInt("bookingid"));
        }
        // No assertion on purpose: run with request logging to observe the behaviour.
        System.out.println("checkout-before-checkin returned HTTP " + response.statusCode());
    }
}
