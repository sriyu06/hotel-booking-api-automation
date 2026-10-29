package com.hotelbooking.clients;

import static io.restassured.RestAssured.given;

import com.hotelbooking.config.ConfigManager;
import com.hotelbooking.models.AuthRequest;
import io.restassured.response.Response;

public class AuthClient {

    /** POST /auth with the given credentials. Returns the raw response so tests can inspect it. */
    public Response login(String username, String password) {
        return given().spec(ApiSpecs.sensitive())
                .body(new AuthRequest(username, password))
                .when()
                .post("/auth");
    }

    /** Logs in with the configured credentials and returns the token. */
    public String fetchToken() {
        Response response = login(ConfigManager.username(), ConfigManager.password());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("POST /auth returned HTTP " + response.statusCode());
        }
        String token = response.jsonPath().getString("token");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "POST /auth returned no token. Check BOOKER_USERNAME / BOOKER_PASSWORD.");
        }
        return token;
    }
}
