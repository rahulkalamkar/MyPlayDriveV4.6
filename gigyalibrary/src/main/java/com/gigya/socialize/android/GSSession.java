package com.gigya.socialize.android;

import com.gigya.socialize.GSObject;

/**
 * Wraps a Gigya session returned from the OAuth2 version of the login process
 */
public class GSSession {
    private String secret;

    public void setSecret(String secret) { this.secret = secret; }

    public String getSecret() { return secret; }

    private String token;

    public void setToken(String value) { this.token = value; }

    public String getToken() { return token; }

    private long expirationTime;

    public void setExpirationTime(long expirationTime) { this.expirationTime = expirationTime;}

    public long getExpirationTime() { return expirationTime; }

    public GSSession() {}

    /**
     * Construct a GSSession using token & secret. The token & secret are received from Gigya in the response of the socialize.notifyLogin call.
     *
     * @param token  a string representing an authorization issued to the client by Gigya server. As long as the token is valid, the client application will have access to Gigya's API.
     * @param secret a string representing a secret key issued to the client by Gigya server.
     */
    public GSSession(String token, String secret) {
        this(token, secret, Long.MAX_VALUE);
    }

    /**
     * Construct a GSSession using token, secret and expiration time. The token & secret are received from Gigya in the response of the socialize.notifyLogin call.
     *
     * @param token             a string representing an authorization issued to the client by Gigya server. As long as the token is valid, the client application will have access to Gigya's API.
     * @param secret            a string representing a secret key issued to the client by Gigya server.
     * @param expirationSeconds number of seconds from now to expire the session. When the expirationTime elapses the session will be considered not valid. pass -1 to set to max value.
     */
    public GSSession(String token, String secret, long expirationSeconds) {
        this.setToken(token);
        this.setSecret(secret);
        if (expirationSeconds == -1 || expirationSeconds == Long.MAX_VALUE)
            this.setExpirationTime(Long.MAX_VALUE);
        else
            this.setExpirationTime(System.currentTimeMillis() + (expirationSeconds * 1000));
    }

    protected GSSession(GSObject params) {
        this(params.getString("access_token", null), params.getString("x_access_token_secret", null), params.getLong("expires_in", -1));
    }

    /**
     * The method checks if the session is valid. If the session is not initializes, it is considered not valid. In addition, when the session's expirationTime elapses the session is considered not valid.
     */
    public boolean isValid() {
        return (getToken() != null && getSecret() != null && System.currentTimeMillis() < getExpirationTime());
    }

}
