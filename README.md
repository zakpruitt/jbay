# jbay

A lightweight Java client for the eBay Sell REST APIs: OAuth token management,
Fulfillment orders, and Finances transactions.

- **One dependency**: [Jackson](https://github.com/FasterXML/jackson-databind) for JSON. HTTP is the JDK's `java.net.http.HttpClient`.
- **Java 21+**, models are plain records.
- OAuth access tokens are minted from your refresh token and cached automatically.
- eBay's 90-day date-filter limit and result paging are handled internally.

## Installation

```xml
<dependency>
    <groupId>com.zakpruitt</groupId>
    <artifactId>jbay</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Package layout

| Package | Contents |
|---|---|
| `com.zakpruitt.jbay` | `Jbay` (entry point), `EbayEnvironment`, `JbayException`, `Amount` |
| `com.zakpruitt.jbay.auth` | `EbayAuth` (consent flow + tokens), `TokenGrant` |
| `com.zakpruitt.jbay.orders` | `OrdersApi`, `Order`, `LineItem`, `Buyer`, `PricingSummary` |
| `com.zakpruitt.jbay.finances` | `FinancesApi`, `Transaction` |
| `com.zakpruitt.jbay.internal` | Implementation details — **not public API**, may change in any release |

## Getting started

Everything starts from a `Jbay` instance. Build one per eBay application and
reuse it — it is thread-safe, and it caches access tokens internally so
repeated calls don't hit the token endpoint.

```java
import com.zakpruitt.jbay.EbayEnvironment;
import com.zakpruitt.jbay.Jbay;

Jbay jbay = Jbay.builder()
        .credentials(clientId, clientSecret)   // required
        .refreshToken(refreshToken)            // required for API calls (see below if you don't have one)
        .environment(EbayEnvironment.PRODUCTION) // the default; use SANDBOX for testing
        .build();
```

`clientId` / `clientSecret` are the app credentials from the
[eBay developer portal](https://developer.ebay.com/my/keys). The refresh token
is per eBay seller account and is obtained once via the consent flow below.

## Getting a refresh token (one-time consent flow)

eBay's OAuth model: the seller approves your app once in a browser, you exchange
the resulting code for a **refresh token** (valid ~18 months), and jbay then
mints short-lived access tokens from it automatically.

The redirect URI must match the "auth accepted URL" (RuName redirect)
configured on your eBay developer application.

```java
import com.zakpruitt.jbay.auth.EbayAuth;
import com.zakpruitt.jbay.auth.TokenGrant;

Jbay jbay = Jbay.builder()
        .credentials(clientId, clientSecret)
        .build(); // no refresh token yet — only the consent-flow methods are usable

// 1. Send the eBay account owner here in a browser:
String url = jbay.auth().consentUrl(redirectUri,
        List.of(EbayAuth.SCOPE_SELL_FULFILLMENT, EbayAuth.SCOPE_SELL_FINANCES));

// 2. After they approve, eBay redirects to your redirectUri with ?code=...
TokenGrant grant = jbay.auth().exchangeAuthorizationCode(code, redirectUri);

// 3. Persist grant.refreshToken() somewhere safe. This jbay instance adopts it
//    immediately; pass it to the builder in future sessions to skip this flow.
```

Request only the scopes you need: `SCOPE_SELL_FULFILLMENT` for orders,
`SCOPE_SELL_FINANCES` for transactions.

## Orders (Fulfillment API)

```java
import com.zakpruitt.jbay.orders.Order;

// Everything created in the last 6 months. Paging and the 90-day
// filter-window limit are handled for you.
List<Order> orders = jbay.orders().since(ZonedDateTime.now().minusMonths(6));

// A single order by its eBay order id.
Order order = jbay.orders().byId("12-34567-89012");

for (Order o : orders) {
    System.out.printf("%s | %s | %s | items=%d | total=%s%n",
            o.orderId(),
            o.creationDateTime(),
            o.buyerUsername(),
            o.lineItems().size(),
            o.pricingSummary().total().asBigDecimal());
}
```

`Order.lineItems()` never returns null (missing lists come back empty), and
`buyerUsername()` is null-safe.

## Transactions (Finances API)

Transactions are the seller's ledger — the money that actually moved, as
opposed to the listed prices on the order. Types include `SALE`, `REFUND`,
`SHIPPING_LABEL`, `NON_SALE_CHARGE`, and `ADJUSTMENT`.

```java
import com.zakpruitt.jbay.finances.Transaction;

List<Transaction> transactions =
        jbay.finances().transactionsSince(ZonedDateTime.now().minusMonths(6));

for (Transaction t : transactions) {
    System.out.printf("%s %s %s fee=%s%n",
            t.transactionDateTime(),
            t.transactionType(),
            t.amount().asBigDecimal(),
            Amount.orZero(t.totalFeeAmount()));
}
```

Join transactions to orders on `Transaction.orderId()` when you need both the
listed prices and the actual money movement.

## Working with money

eBay returns money as a decimal string plus currency code, and jbay keeps it
that way in `Amount`:

```java
Amount price = order.pricingSummary().total();

price.asBigDecimal();     // BigDecimal — use this for arithmetic
price.asDouble();         // convenience, when precision doesn't matter
price.currency();         // e.g. "USD"
Amount.orZero(maybeNull); // 0.0 for null amounts — handy for optional fields
```

Null, blank, or unparseable values become `BigDecimal.ZERO` rather than
throwing.

## Error handling

Everything throws `JbayException` (unchecked) — configuration mistakes,
transport failures, and eBay error responses alike. For eBay error responses
it carries the HTTP status and the raw body:

```java
try {
    jbay.orders().byId(orderId);
} catch (JbayException e) {
    if (e.statusCode() == 404) {
        // no such order
    } else {
        log.error("eBay call failed ({}): {}", e.statusCode(), e.responseBody(), e);
    }
}
```

`statusCode()` is `-1` when the failure happened before a response arrived
(connection refused, timeout, etc.).

## Sandbox

Pass `.environment(EbayEnvironment.SANDBOX)` to the builder. Note that sandbox
uses its own app credentials, its own consent URL, and its own refresh tokens —
nothing carries over from production.

## License

[MIT](LICENSE)
