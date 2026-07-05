/**
 * jbay — a lightweight Java client for the eBay Sell REST APIs.
 *
 * <p>No framework dependencies: built on {@link java.net.http.HttpClient} and Jackson.
 * OAuth token minting, refreshing, and caching are handled automatically.
 *
 * <p>This root package holds the entry point ({@link com.zakpruitt.jbay.Jbay}) and the types
 * shared across the API packages. The rest of the library:
 *
 * <ul>
 *   <li>{@link com.zakpruitt.jbay.auth} — OAuth consent flow and token grants</li>
 *   <li>{@link com.zakpruitt.jbay.orders} — Fulfillment API orders</li>
 *   <li>{@link com.zakpruitt.jbay.finances} — Finances API transactions</li>
 *   <li>{@code com.zakpruitt.jbay.internal} — implementation details, not public API</li>
 * </ul>
 *
 * <pre>{@code
 * Jbay jbay = Jbay.builder()
 *         .credentials(clientId, clientSecret)
 *         .refreshToken(refreshToken)
 *         .environment(EbayEnvironment.PRODUCTION)
 *         .build();
 *
 * List<Order> orders = jbay.orders().since(ZonedDateTime.now().minusMonths(6));
 * List<Transaction> transactions = jbay.finances().transactionsSince(ZonedDateTime.now().minusMonths(6));
 * }</pre>
 *
 * <p>If you don't have a refresh token yet, bootstrap one with the consent flow:
 *
 * <pre>{@code
 * String url = jbay.auth().consentUrl(redirectUri, List.of(EbayAuth.SCOPE_SELL_FULFILLMENT));
 * // user approves in browser, eBay redirects back with ?code=...
 * TokenGrant grant = jbay.auth().exchangeAuthorizationCode(code, redirectUri);
 * // persist grant.refreshToken() — jbay uses it for this session automatically
 * }</pre>
 */
package com.zakpruitt.jbay;
