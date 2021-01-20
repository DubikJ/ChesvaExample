package ua.com.expertsolution.chesva.model.json;

import com.google.gson.annotations.SerializedName;

public class MainAssetRequest {
    @SerializedName("Take")
    private int take;
    @SerializedName("skip")
    private int skip;

    public MainAssetRequest(int take, int skip) {
        this.take = take;
        this.skip = skip;
    }

    public int getTake() {
        return take;
    }

    public void setTake(int take) {
        this.take = take;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }
}
