package ua.com.expertsolution.chesva.model.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.expertsolution.chesva.model.dto.MainAsset;
import ua.com.expertsolution.chesva.model.dto.Operation;

public class ChangeMainAssetRfidListRequest {

    @SerializedName("Items")
    private List<Operation> items;

    public ChangeMainAssetRfidListRequest(List<Operation> items) {
        this.items = items;
    }

    public List<Operation> getItems() {
        return items;
    }

    public void setItems(List<Operation> items) {
        this.items = items;
    }
}
