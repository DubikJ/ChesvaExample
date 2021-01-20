package ua.com.expertsolution.chesva.model.json;

import com.google.gson.annotations.SerializedName;

public class AuthRequest {
    @SerializedName("grant_type")
    private String grantType;
    @SerializedName("username")
    private String userName;
    @SerializedName("password")
    private String password;

    public AuthRequest(String userName, String password) {
        this.grantType = "password";
        this.userName = userName;
        this.password = password;
    }

    public AuthRequest(String grantType, String userName, String password) {
        this.grantType = grantType;
        this.userName = userName;
        this.password = password;
    }
}
