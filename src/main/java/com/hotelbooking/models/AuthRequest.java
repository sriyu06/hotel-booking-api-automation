package com.hotelbooking.models;

/** Body of POST /auth. */
public record AuthRequest(String username, String password) {
}
