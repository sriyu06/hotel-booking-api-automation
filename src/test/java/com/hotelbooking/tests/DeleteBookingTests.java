package com.hotelbooking.tests;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

import com.hotelbooking.base.BaseTest;
import com.hotelbooking.utils.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeleteBookingTests extends BaseTest {

    @Test
    @DisplayName("DELETE with a token returns 201 and the booking can no longer be fetched")
    void delete_withToken_removesBooking() {
        int id = createBooking(TestDataFactory.validBooking());

        // Restful-Booker documents 201 Created for a successful delete.
        bookings.delete(id, token()).then().statusCode(201);

        bookings.get(id).then().statusCode(404);
    }

    @Test
    @DisplayName("DELETE without a token is forbidden and the booking survives")
    void delete_withoutToken_isForbidden() {
        int id = createBooking(TestDataFactory.validBooking());

        bookings.delete(id, null).then().statusCode(403);

        bookings.get(id).then().statusCode(200);
    }

    @Test
    @DisplayName("DELETE with an invalid token is forbidden")
    void delete_withInvalidToken_isForbidden() {
        int id = createBooking(TestDataFactory.validBooking());

        bookings.delete(id, "invalid-token").then().statusCode(403);
    }

    @Test
    @DisplayName("DELETE on an id that does not exist fails")
    void delete_nonexistentId_fails() {
        // Restful-Booker answers 405 here (404 would be the textbook answer); accept both.
        bookings.delete(999_999_999, token()).then().statusCode(anyOf(is(404), is(405)));
    }
}
