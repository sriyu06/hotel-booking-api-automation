package com.hotelbooking.tests;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hotelbooking.base.BaseTest;
import com.hotelbooking.models.Booking;
import com.hotelbooking.utils.TestDataFactory;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateBookingTests extends BaseTest {

    @Test
    @DisplayName("PUT with a token replaces the whole booking")
    void put_updatesAllFields() {
        int id = createBooking(TestDataFactory.validBooking());
        Booking update = TestDataFactory.updatedBooking();

        Booking putResponse = bookings.put(id, update, token())
                .then().statusCode(200)
                .extract().as(Booking.class);
        assertEquals(update, putResponse, "PUT response should echo the new data");

        Booking stored = bookings.get(id).then().statusCode(200).extract().as(Booking.class);
        assertEquals(update, stored, "GET after PUT should return the new data");
    }

    @Test
    @DisplayName("PATCH with a token changes only the supplied fields")
    void patch_updatesOnlySuppliedFields() {
        Booking original = TestDataFactory.validBooking();
        int id = createBooking(original);

        bookings.patch(id, Map.of("firstname", "Patched", "totalprice", 999), token())
                .then()
                .statusCode(200)
                .body("firstname", equalTo("Patched"))
                .body("totalprice", equalTo(999))
                .body("lastname", equalTo(original.lastname()));

        bookings.get(id).then().statusCode(200)
                .body("firstname", equalTo("Patched"))
                .body("lastname", equalTo(original.lastname()));
    }

    @Test
    @DisplayName("PUT without a token is forbidden (403)")
    void put_withoutToken_isForbidden() {
        int id = createBooking(TestDataFactory.validBooking());

        bookings.put(id, TestDataFactory.updatedBooking(), null).then().statusCode(403);
    }

    @Test
    @DisplayName("PATCH without a token is forbidden (403)")
    void patch_withoutToken_isForbidden() {
        int id = createBooking(TestDataFactory.validBooking());

        bookings.patch(id, Map.of("firstname", "Hacker"), null).then().statusCode(403);
    }

    @Test
    @DisplayName("PUT with an incomplete payload is rejected")
    void put_withMissingFields_isRejected() {
        int id = createBooking(TestDataFactory.validBooking());

        bookings.put(id, Map.of("firstname", "OnlyThis"), token())
                .then()
                .statusCode(rejected());
    }

    @Test
    @DisplayName("PUT on a booking id that does not exist fails")
    void put_onNonexistentId_fails() {
        // Restful-Booker answers 405 here (404 would be the textbook answer); accept both.
        bookings.put(999_999_999, TestDataFactory.updatedBooking(), token())
                .then()
                .statusCode(anyOf(is(404), is(405)));
    }
}
