package com.hotelbooking.clients;

import static io.restassured.RestAssured.given;

import io.restassured.response.Response;

public class HealthClient {

    /** GET /ping - documented health check. */
    public Response ping() {
        return given().spec(ApiSpecs.base()).when().get("/ping");
    }
}
