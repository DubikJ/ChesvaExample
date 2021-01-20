package ua.com.expertsolution.chesva.model.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.expertsolution.chesva.model.dto.Box;
import ua.com.expertsolution.chesva.model.dto.Operation;

public class ChangeBoxRfidListRequest {

    @SerializedName("Items")
    private List<Operation> items;

    public ChangeBoxRfidListRequest(List<Operation> items) {
        this.items = items;
    }

    public List<Operation> getItems() {
        return items;
    }

    public void setItems(List<Operation> items) {
        this.items = items;
    }
}
