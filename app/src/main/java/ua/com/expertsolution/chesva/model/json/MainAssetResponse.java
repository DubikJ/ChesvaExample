package ua.com.expertsolution.chesva.model.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.expertsolution.chesva.model.dto.MainAsset;

public class MainAssetResponse extends DownloadResponse{
    @SerializedName("Total")
    private int total;
    @SerializedName("MainAssets")
    private List<MainAsset> mainAssets;

    public MainAssetResponse(String code, String message, int total, List<MainAsset> mainAssets) {
        super(code, message);
        this.total = total;
        this.mainAssets = mainAssets;
    }

    public int getTotal() {
        return total;
    }

    public List<MainAsset> getMainAssets() {
        return mainAssets;
    }
}
