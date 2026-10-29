package com.hotelbooking.base;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

import com.hotelbooking.clients.AuthClient;
import com.hotelbooking.clients.BookingClient;
import com.hotelbooking.models.Booking;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Timeout;

/**
 * Shared test plumbing:
 *  - one auth token per test run (fetched lazily)
 *  - tracking and clean-up of every booking a test creates
 *  - a per-test timeout so a hung public server cannot block the run forever
 */
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public abstract class BaseTest {

    protected static final BookingClient bookings = new BookingClient();

    private static String cachedToken;
    private final List<Integer> createdIds = new ArrayList<>();

    protected static synchronized String token() {
        if (cachedToken == null) {
            cachedToken = new AuthClient().fetchToken();
        }
        return cachedToken;
    }

    /** Creates a booking, asserts it worked, and registers it for clean-up. */
    protected int createBooking(Booking booking) {
        Response response = bookings.create(booking);
        response.then().statusCode(200);
        int id = response.jsonPath().getInt("bookingid");
        createdIds.add(id);
        return id;
    }

    /** Registers a booking id created outside createBooking() so it is still cleaned up. */
    protected void track(int id) {
        createdIds.add(id);
    }

    /** The public API is not consistent about 400 vs 500 for bad input, so we accept either. */
    protected static Matcher<Integer> rejected() {
        return anyOf(is(400), is(500));
    }

    @AfterEach
    void cleanUpCreatedBookings() {
        for (int id : createdIds) {
            try {
                bookings.delete(id, token());
            } catch (RuntimeException ignored) {
                // best-effort clean-up: the test may already have deleted the booking
            }
        }
        createdIds.clear();
    }
}
