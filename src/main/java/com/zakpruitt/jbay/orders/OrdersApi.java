package com.zakpruitt.jbay.orders;

import com.zakpruitt.jbay.EbayEnvironment;
import com.zakpruitt.jbay.auth.EbayAuth;
import com.zakpruitt.jbay.internal.EbayHttp;
import com.zakpruitt.jbay.internal.Pages;

import java.time.ZonedDateTime;
import java.util.List;

/** Read access to orders via the eBay Fulfillment API. */
public final class OrdersApi {

    private static final int PAGE_SIZE = 50;

    private final EbayHttp http;
    private final EbayAuth auth;
    private final Pages pages;
    private final String baseUrl;

    /** Wired by {@link com.zakpruitt.jbay.Jbay#builder()} — not intended to be constructed directly. */
    public OrdersApi(EbayHttp http, EbayAuth auth, EbayEnvironment environment) {
        this.http = http;
        this.auth = auth;
        this.pages = new Pages(http, auth);
        this.baseUrl = environment.apiBaseUrl() + "/sell/fulfillment/v1/order";
    }

    /** All orders created since the given moment. Paging and eBay's 90-day filter limit are handled internally. */
    public List<Order> since(ZonedDateTime since) {
        return pages.fetchAllSince(baseUrl, PAGE_SIZE, "creationdate", since, OrdersPage.class, OrdersPage::orders);
    }

    public Order byId(String orderId) {
        return http.get(baseUrl + "/" + EbayHttp.encode(orderId), auth.accessToken(), Order.class);
    }
}

record OrdersPage(List<Order> orders) {
}
