package com.zakpruitt.jbay.finances;

import com.zakpruitt.jbay.EbayEnvironment;
import com.zakpruitt.jbay.auth.EbayAuth;
import com.zakpruitt.jbay.internal.EbayHttp;
import com.zakpruitt.jbay.internal.Pages;

import java.time.ZonedDateTime;
import java.util.List;

/** Read access to payout transactions via the eBay Finances API. */
public final class FinancesApi {

    private static final int PAGE_SIZE = 200;

    private final Pages pages;
    private final String baseUrl;

    /** Wired by {@link com.zakpruitt.jbay.Jbay#builder()} — not intended to be constructed directly. */
    public FinancesApi(EbayHttp http, EbayAuth auth, EbayEnvironment environment) {
        this.pages = new Pages(http, auth);
        this.baseUrl = environment.apiBaseUrl() + "/sell/finances/v1/transaction";
    }

    /** All transactions since the given moment. Paging and eBay's 90-day filter limit are handled internally. */
    public List<Transaction> transactionsSince(ZonedDateTime since) {
        return pages.fetchAllSince(baseUrl, PAGE_SIZE, "transactionDate", since,
                TransactionsPage.class, TransactionsPage::transactions);
    }
}

record TransactionsPage(List<Transaction> transactions) {
}
