package com.zakpruitt.jbay;

import com.zakpruitt.jbay.auth.EbayAuth;
import com.zakpruitt.jbay.finances.FinancesApi;
import com.zakpruitt.jbay.internal.EbayHttp;
import com.zakpruitt.jbay.orders.OrdersApi;

/**
 * Entry point to jbay. Build one instance per eBay application and reuse it;
 * it is thread-safe and caches access tokens internally.
 *
 * <pre>{@code
 * Jbay jbay = Jbay.builder()
 *         .credentials(clientId, clientSecret)
 *         .refreshToken(refreshToken)
 *         .build();
 * }</pre>
 */
public final class Jbay {

    private final EbayAuth auth;
    private final OrdersApi orders;
    private final FinancesApi finances;

    private Jbay(Builder builder) {
        EbayHttp http = new EbayHttp();
        this.auth = new EbayAuth(http, builder.environment, builder.clientId, builder.clientSecret, builder.refreshToken);
        this.orders = new OrdersApi(http, auth, builder.environment);
        this.finances = new FinancesApi(http, auth, builder.environment);
    }

    public static Builder builder() {
        return new Builder();
    }

    public EbayAuth auth() {
        return auth;
    }

    public OrdersApi orders() {
        return orders;
    }

    public FinancesApi finances() {
        return finances;
    }

    public static final class Builder {

        private String clientId;
        private String clientSecret;
        private String refreshToken;
        private EbayEnvironment environment = EbayEnvironment.PRODUCTION;

        private Builder() {
        }

        public Builder credentials(String clientId, String clientSecret) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            return this;
        }

        /** Optional at build time — without it, only the consent-flow methods on {@link EbayAuth} are usable. */
        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder environment(EbayEnvironment environment) {
            this.environment = environment;
            return this;
        }

        public Jbay build() {
            if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
                throw new JbayException("clientId and clientSecret are required");
            }
            return new Jbay(this);
        }
    }
}
