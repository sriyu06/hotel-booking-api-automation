package com.hotelbooking.clients;

import com.hotelbooking.config.ConfigManager;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.Set;

/**
 * Reusable request specifications (base URI, headers, timeouts, logging).
 */
public final class ApiSpecs {

    /** Headers that must never appear in logs. */
    private static final Set<String> REDACTED_HEADERS = Set.of("Cookie", "Set-Cookie", "Authorization");

    private ApiSpecs() {
    }

    private static HttpClientConfig timeouts() {
        return HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", ConfigManager.connectTimeoutMs())
                .setParam("http.socket.timeout", ConfigManager.readTimeoutMs());
    }

    /**
     * Normal spec. If an assertion fails, the full request and response are printed,
     * with Cookie/Authorization headers redacted.
     */
    public static RequestSpecification base() {
        RestAssuredConfig config = RestAssuredConfig.config()
                .httpClient(timeouts())
                .logConfig(LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
                        .blacklistHeaders(REDACTED_HEADERS));
        return builder(config).build();
    }

    /**
     * Spec for calls whose body contains credentials or tokens (POST /auth).
     * Nothing is logged on failure, so passwords and tokens never reach the console.
     */
    public static RequestSpecification sensitive() {
        RestAssuredConfig config = RestAssuredConfig.config().httpClient(timeouts());
        return builder(config).build();
    }

    private static RequestSpecBuilder builder(RestAssuredConfig config) {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.baseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(config);
    }
}
