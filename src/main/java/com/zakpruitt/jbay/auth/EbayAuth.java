package com.zakpruitt.jbay.auth;

import com.zakpruitt.jbay.EbayEnvironment;
import com.zakpruitt.jbay.JbayException;
import com.zakpruitt.jbay.internal.EbayHttp;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

/**
 * Handles the eBay OAuth lifecycle: bootstrapping a refresh token via the user consent flow,
 * then minting and caching short-lived access tokens from it.
 */
public final class EbayAuth {

    public static final String SCOPE_SELL_FULFILLMENT = "https://api.ebay.com/oauth/api_scope/sell.fulfillment";
    public static final String SCOPE_SELL_FINANCES = "https://api.ebay.com/oauth/api_scope/sell.finances";

    private static final Duration EXPIRY_BUFFER = Duration.ofSeconds(60);

    private final EbayHttp http;
    private final EbayEnvironment environment;
    private final String clientId;
    private final String clientSecret;

    private String refreshToken;
    private String accessToken;
    private Instant accessTokenExpiry;

    /**
     * Wired by {@link com.zakpruitt.jbay.Jbay#builder()} —
     * not intended to be constructed directly.
     */
    public EbayAuth(EbayHttp http, EbayEnvironment environment, String clientId, String clientSecret, String refreshToken) {
        this.http = http;
        this.environment = environment;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
    }

    /**
     * A valid access token, minted from the refresh token and cached until shortly before expiry.
     * The API clients call this before every request, so callers rarely need it directly.
     */
    public synchronized String accessToken() {
        if (accessToken != null && Instant.now().isBefore(accessTokenExpiry)) {
            return accessToken;
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new JbayException("No refresh token configured. Either pass one to the builder, or send the "
                    + "user to consentUrl(...) and call exchangeAuthorizationCode(...) with the returned code.");
        }
        cache(tokenRequest(Map.of(
                "grant_type", "refresh_token",
                "refresh_token", refreshToken)));
        return accessToken;
    }

    /**
     * The URL to send the eBay account owner to for one-time consent. The redirect URI must match
     * the "auth accepted URL" (RuName redirect) configured on the eBay developer application.
     */
    public String consentUrl(String redirectUri, Collection<String> scopes) {
        return environment.consentBaseUrl()
                + "?response_type=code"
                + "&client_id=" + EbayHttp.encode(clientId)
                + "&redirect_uri=" + EbayHttp.encode(redirectUri)
                + "&scope=" + EbayHttp.encode(String.join(" ", scopes));
    }

    /**
     * Exchanges the authorization code from the consent redirect for a long-lived refresh token.
     * The grant is adopted for this instance immediately; persist {@link TokenGrant#refreshToken()}
     * yourself so future sessions can skip the consent flow.
     */
    public synchronized TokenGrant exchangeAuthorizationCode(String code, String redirectUri) {
        TokenGrant grant = tokenRequest(Map.of(
                "grant_type", "authorization_code",
                "code", code,
                "redirect_uri", redirectUri));
        refreshToken = grant.refreshToken();
        cache(grant);
        return grant;
    }

    private TokenGrant tokenRequest(Map<String, String> form) {
        TokenGrant grant = http.postForm(environment.tokenUrl(), clientId, clientSecret, form, TokenGrant.class);
        if (grant == null || grant.accessToken() == null) {
            throw new JbayException("eBay token endpoint returned no access token");
        }
        return grant;
    }

    private void cache(TokenGrant grant) {
        accessToken = grant.accessToken();
        accessTokenExpiry = Instant.now().plusSeconds(grant.expiresIn()).minus(EXPIRY_BUFFER);
    }
}
