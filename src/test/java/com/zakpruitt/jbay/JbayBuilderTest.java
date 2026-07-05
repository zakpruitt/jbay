package com.zakpruitt.jbay;

import com.zakpruitt.jbay.auth.EbayAuth;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JbayBuilderTest {

    @Test
    void buildsWithCredentials() {
        Jbay jbay = Jbay.builder()
                .credentials("client-id", "client-secret")
                .build();

        assertNotNull(jbay.auth());
        assertNotNull(jbay.orders());
        assertNotNull(jbay.finances());
    }

    @Test
    void rejectsMissingCredentials() {
        assertThrows(JbayException.class, () -> Jbay.builder().build());
    }

    @Test
    void rejectsBlankClientId() {
        assertThrows(JbayException.class,
                () -> Jbay.builder().credentials("   ", "client-secret").build());
    }

    @Test
    void rejectsNullClientSecret() {
        assertThrows(JbayException.class,
                () -> Jbay.builder().credentials("client-id", null).build());
    }

    @Test
    void refreshTokenIsOptionalAtBuildTime() {
        Jbay jbay = Jbay.builder()
                .credentials("client-id", "client-secret")
                .environment(EbayEnvironment.SANDBOX)
                .build();

        // Usable for the consent flow without a refresh token...
        assertNotNull(jbay.auth().consentUrl("https://example.com/callback",
                List.of(EbayAuth.SCOPE_SELL_FULFILLMENT)));

        // ...but minting an access token requires one.
        assertThrows(JbayException.class, () -> jbay.auth().accessToken());
    }
}
