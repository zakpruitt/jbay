package com.zakpruitt.jbay;

/** The eBay deployment to talk to. Sandbox credentials and tokens are separate from production ones. */
public enum EbayEnvironment {

    PRODUCTION("https://api.ebay.com", "https://auth.ebay.com/oauth2/authorize"),
    SANDBOX("https://api.sandbox.ebay.com", "https://auth.sandbox.ebay.com/oauth2/authorize");

    private final String apiBaseUrl;
    private final String consentBaseUrl;

    EbayEnvironment(String apiBaseUrl, String consentBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
        this.consentBaseUrl = consentBaseUrl;
    }

    public String apiBaseUrl() {
        return apiBaseUrl;
    }

    public String consentBaseUrl() {
        return consentBaseUrl;
    }

    public String tokenUrl() {
        return apiBaseUrl + "/identity/v1/oauth2/token";
    }
}
