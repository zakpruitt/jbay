package com.zakpruitt.jbay.orders;

import java.time.ZonedDateTime;
import java.util.List;

/** An order from the eBay Fulfillment API ({@code /sell/fulfillment/v1/order}). */
public record Order(
        String orderId,
        String creationDate,
        String orderFulfillmentStatus,
        String orderPaymentStatus,
        PricingSummary pricingSummary,
        List<LineItem> lineItems,
        Buyer buyer) {

    public ZonedDateTime creationDateTime() {
        return creationDate == null ? null : ZonedDateTime.parse(creationDate);
    }

    @Override
    public List<LineItem> lineItems() {
        return lineItems == null ? List.of() : lineItems;
    }

    public String buyerUsername() {
        return buyer == null || buyer.username() == null ? "" : buyer.username();
    }
}
