package ua.com.expertsolution.chesva.model.json;

import com.google.gson.annotations.SerializedName;

public class DownloadResponse {

    @SerializedName("Code")
    private String code;
    @SerializedName("Message")
    private String message;



    public DownloadResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
