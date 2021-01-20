package ua.com.expertsolution.chesva.model.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.expertsolution.chesva.model.dto.Box;

public class BoxResponse extends DownloadResponse{
    @SerializedName("Total")
    private int total;
    @SerializedName("Boxes")
    private List<Box> boxes;

    public BoxResponse(String code, String message, int total, List<Box> boxes) {
        super(code, message);
        this.total = total;
        this.boxes = boxes;
    }

    public int getTotal() {
        return total;
    }

    public List<Box> getBoxes() {
        return boxes;
    }
}
