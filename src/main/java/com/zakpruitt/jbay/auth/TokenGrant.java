package com.zakpruitt.jbay.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Response from the eBay OAuth token endpoint. {@code refreshToken} is only present on the authorization-code grant. */
public record TokenGrant(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") long expiresIn,
        @JsonProperty("refresh_token_expires_in") long refreshTokenExpiresIn,
        @JsonProperty("token_type") String tokenType) {
}
