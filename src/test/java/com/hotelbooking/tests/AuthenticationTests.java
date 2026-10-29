package com.hotelbooking.tests;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hotelbooking.base.BaseTest;
import com.hotelbooking.clients.AuthClient;
import com.hotelbooking.config.ConfigManager;
import com.hotelbooking.models.Booking;
import com.hotelbooking.utils.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AuthenticationTests extends BaseTest {

    private final AuthClient auth = new AuthClient();

    @Test
    @DisplayName("Valid credentials return 200 and a token")
    void validCredentials_returnToken() {
        auth.login(ConfigManager.username(), ConfigManager.password())
                .then()
                .statusCode(200)
                .body("token", not(emptyOrNullString()));
    }

    // Restful-Booker answers wrong credentials with HTTP 200 and {"reason":"Bad credentials"}.
    @ParameterizedTest(name = "[{index}] user={0}")
    @DisplayName("Invalid credentials return 'Bad credentials' and no token")
    @CsvSource({
            "admin,wrong-password",
            "not-a-user,password123",
            "not-a-user,wrong-password"
    })
    void invalidCredentials_returnBadCredentials(String username, String password) {
        auth.login(username, password)
                .then()
                .statusCode(200)
                .body("reason", equalTo("Bad credentials"))
                .body("token", nullValue());
    }

    @Test
    @DisplayName("Update without a token is forbidden (403) and leaves the booking unchanged")
    void protectedEndpoint_withoutToken_isForbidden() {
        Booking original = TestDataFactory.validBooking();
        int id = createBooking(original);

        bookings.put(id, TestDataFactory.updatedBooking(), null)
                .then()
                .statusCode(403);

        Booking afterAttempt = bookings.get(id).then().statusCode(200).extract().as(Booking.class);
        assertEquals(original, afterAttempt, "booking must not change after an unauthorised update");
    }

    @Test
    @DisplayName("Update with an invalid token is forbidden (403)")
    void protectedEndpoint_withInvalidToken_isForbidden() {
        int id = createBooking(TestDataFactory.validBooking());

        bookings.put(id, TestDataFactory.updatedBooking(), "this-is-not-a-valid-token")
                .then()
                .statusCode(403);
    }
}
