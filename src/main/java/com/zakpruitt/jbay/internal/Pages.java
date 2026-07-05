package com.zakpruitt.jbay.internal;

import com.zakpruitt.jbay.auth.EbayAuth;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Paging helper. eBay date-range filters are capped at 90 days, so a "since" query
 * is split into consecutive windows, each drained page by page. Not part of the public API.
 */
public final class Pages {

    private static final int WINDOW_DAYS = 90;
    private static final DateTimeFormatter EBAY_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final EbayHttp http;
    private final EbayAuth auth;

    public Pages(EbayHttp http, EbayAuth auth) {
        this.http = http;
        this.auth = auth;
    }

    public <P, T> List<T> fetchAllSince(String baseUrl, int pageSize, String dateField, ZonedDateTime since,
                                        Class<P> pageType, Function<P, List<T>> itemsOf) {
        List<T> all = new ArrayList<>();
        for (String filter : timeWindowFilters(dateField, since, ZonedDateTime.now(ZoneOffset.UTC))) {
            all.addAll(fetchAllPages(baseUrl, pageSize, filter, pageType, itemsOf));
        }
        return all;
    }

    private <P, T> List<T> fetchAllPages(String baseUrl, int pageSize, String filter,
                                         Class<P> pageType, Function<P, List<T>> itemsOf) {
        List<T> all = new ArrayList<>();
        for (int offset = 0; ; offset += pageSize) {
            String url = baseUrl
                    + "?limit=" + pageSize
                    + "&offset=" + offset
                    + "&filter=" + EbayHttp.encode(filter);

            P page = http.get(url, auth.accessToken(), pageType);
            if (page == null) {
                return all;
            }

            List<T> items = itemsOf.apply(page);
            if (items == null || items.isEmpty()) {
                return all;
            }

            all.addAll(items);
            if (items.size() < pageSize) {
                return all;
            }
        }
    }

    static List<String> timeWindowFilters(String dateField, ZonedDateTime since, ZonedDateTime now) {
        List<String> filters = new ArrayList<>();
        ZonedDateTime start = since.withZoneSameInstant(ZoneOffset.UTC);

        for (; start.isBefore(now); start = start.plusDays(WINDOW_DAYS)) {
            ZonedDateTime end = start.plusDays(WINDOW_DAYS);
            if (end.isAfter(now)) {
                end = now;
            }
            filters.add(dateField + ":[" + start.format(EBAY_DATE) + ".." + end.format(EBAY_DATE) + "]");
        }
        return filters;
    }
}
