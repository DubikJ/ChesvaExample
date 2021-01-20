package ua.com.expertsolution.chesva.model.json;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("error")
    private String error;
    @SerializedName("error_description")
    private String errorDescription;
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("expires_in")
    private String expiresIn;
    @SerializedName("userName")
    private String userName;
    @SerializedName(".issued")
    private String issued;
    @SerializedName(".expires")
    private String expires;

    public AuthResponse(String error, String errorDescription, String accessToken,
                        String tokenType, String expiresIn, String userName, String issued, String expires) {
        this.error = error;
        this.errorDescription = errorDescription;
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.userName = userName;
        this.issued = issued;
        this.expires = expires;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public String getUserName() {
        return userName;
    }

    public String getIssued() {
        return issued;
    }

    public String getExpires() {
        return expires;
    }
}
