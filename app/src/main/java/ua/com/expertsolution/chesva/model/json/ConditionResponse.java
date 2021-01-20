package ua.com.expertsolution.chesva.model.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.expertsolution.chesva.model.dto.Condition;

public class ConditionResponse extends DownloadResponse{

    @SerializedName("Total")
    private int total;
    @SerializedName("Conditions")
    private List<Condition> conditions;

    public ConditionResponse(String code, String message, int total, List<Condition> conditions) {
        super(code, message);
        this.total = total;
        this.conditions = conditions;
    }

    public int getTotal() {
        return total;
    }

    public List<Condition> getConditions() {
        return conditions;
    }
}
