package com.hotelbooking.tests;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

import com.hotelbooking.base.BaseTest;
import com.hotelbooking.clients.HealthClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HealthCheckTests extends BaseTest {

    private final HealthClient health = new HealthClient();

    @Test
    @DisplayName("GET /ping returns 201 Created (documented behaviour of Restful-Booker)")
    void ping_returns201() {
        health.ping()
                .then()
                .statusCode(201)
                .body(equalTo("Created"))
                .time(lessThan(15_000L)); // generous: free hosting can be slow on a cold start
    }
}
