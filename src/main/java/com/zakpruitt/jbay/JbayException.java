package com.zakpruitt.jbay;

/**
 * The single exception type thrown by jbay: configuration errors, transport failures,
 * and eBay error responses (which carry {@link #statusCode()} and {@link #responseBody()}).
 */
public class JbayException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public JbayException(String message) {
        this(message, null);
    }

    public JbayException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.responseBody = null;
    }

    public JbayException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    /** HTTP status of the failed call, or -1 if the failure happened before a response arrived. */
    public int statusCode() {
        return statusCode;
    }

    public String responseBody() {
        return responseBody;
    }
}
