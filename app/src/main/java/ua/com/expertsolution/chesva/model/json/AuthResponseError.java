package ua.com.expertsolution.chesva.model.json;

import com.google.gson.annotations.SerializedName;

public class AuthResponseError{

    @SerializedName("error")
    private String error;
    @SerializedName("error_description")
    private String errorDescription;

    public AuthResponseError(String error, String errorDescription) {
        this.error = error;
        this.errorDescription = errorDescription;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}
