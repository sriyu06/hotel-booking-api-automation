package com.hotelbooking.clients;

import static io.restassured.RestAssured.given;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;

/**
 * Thin wrapper around the /booking endpoints. Every method returns the raw Response,
 * so the tests (not the client) decide what to assert.
 * Pass token = null to call a protected endpoint WITHOUT authorisation.
 */
public class BookingClient {

    private static final String BOOKING = "/booking";
    private static final String BOOKING_BY_ID = "/booking/{id}";

    /** POST /booking. body can be a Booking, a Map, or a raw JSON String (sent as-is). */
    public Response create(Object body) {
        return given().spec(ApiSpecs.base()).body(body).when().post(BOOKING);
    }

    /** GET /booking - all ids. */
    public Response list() {
        return given().spec(ApiSpecs.base()).when().get(BOOKING);
    }

    /** GET /booking?firstname=..&lastname=..&checkin=..&checkout=.. */
    public Response list(Map<String, ?> filters) {
        return given().spec(ApiSpecs.base()).queryParams(filters).when().get(BOOKING);
    }

    /** GET /booking/{id}. id is Object so tests can also pass invalid ids such as "abc". */
    public Response get(Object id) {
        return given().spec(ApiSpecs.base()).when().get(BOOKING_BY_ID, id);
    }

    /** PUT /booking/{id} - full update. Needs the token cookie. */
    public Response put(Object id, Object body, String token) {
        return withAuth(token).body(body).when().put(BOOKING_BY_ID, id);
    }

    /** PATCH /booking/{id} - partial update. Needs the token cookie. */
    public Response patch(Object id, Object body, String token) {
        return withAuth(token).body(body).when().patch(BOOKING_BY_ID, id);
    }

    /** DELETE /booking/{id}. Needs the token cookie. */
    public Response delete(Object id, String token) {
        return withAuth(token).when().delete(BOOKING_BY_ID, id);
    }

    private RequestSpecification withAuth(String token) {
        RequestSpecification request = given().spec(ApiSpecs.base());
        if (token != null) {
            request = request.cookie("token", token);
        }
        return request;
    }
}
