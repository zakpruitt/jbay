package com.zakpruitt.jbay;

/** The eBay deployment to talk to. Sandbox credentials and tokens are separate from production ones. */
public enum EbayEnvironment {

    PRODUCTION("https://api.ebay.com", "https://apiz.ebay.com", "https://auth.ebay.com/oauth2/authorize"),
    SANDBOX("https://api.sandbox.ebay.com", "https://apiz.sandbox.ebay.com", "https://auth.sandbox.ebay.com/oauth2/authorize");

    private final String apiBaseUrl;
    private final String financesApiBaseUrl;
    private final String consentBaseUrl;

    EbayEnvironment(String apiBaseUrl, String financesApiBaseUrl, String consentBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
        this.financesApiBaseUrl = financesApiBaseUrl;
        this.consentBaseUrl = consentBaseUrl;
    }

    public String apiBaseUrl() {
        return apiBaseUrl;
    }

    /** The Finances API is served from the apiz gateway, not api — api.ebay.com 404s for it. */
    public String financesApiBaseUrl() {
        return financesApiBaseUrl;
    }

    public String consentBaseUrl() {
        return consentBaseUrl;
    }

    public String tokenUrl() {
        return apiBaseUrl + "/identity/v1/oauth2/token";
    }
}
