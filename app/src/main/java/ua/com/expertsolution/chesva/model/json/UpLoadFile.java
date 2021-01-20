package ua.com.expertsolution.chesva.model.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.expertsolution.chesva.model.dto.Operation;

public class UpLoadFile {
    @SerializedName("Code")
    private String code;
    @SerializedName("UserID")
    private int userID;
    @SerializedName("BoxesRfid")
    private List<Operation> boxesRfid;
    @SerializedName("ChangePersons")
    private List<Operation> changePersons;
    @SerializedName("ChangeBoxes")
    private List<Operation> changeBoxes;
    @SerializedName("ChangeMainAssetRfids")
    private List<Operation> changeMainAssetRfids;

    public UpLoadFile(String code, int userID, List<Operation> boxesRfid,
                      List<Operation> changePersons, List<Operation> changeBoxes, List<Operation> changeMainAssetRfids) {
        this.code = code;
        this.userID = userID;
        this.boxesRfid = boxesRfid;
        this.changePersons = changePersons;
        this.changeBoxes = changeBoxes;
        this.changeMainAssetRfids = changeMainAssetRfids;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public List<Operation> getBoxesRfid() {
        return boxesRfid;
    }

    public void setBoxesRfid(List<Operation> boxesRfid) {
        this.boxesRfid = boxesRfid;
    }

    public List<Operation> getChangePersons() {
        return changePersons;
    }

    public void setChangePersons(List<Operation> changePersons) {
        this.changePersons = changePersons;
    }

    public List<Operation> getChangeBoxes() {
        return changeBoxes;
    }

    public void setChangeBoxes(List<Operation> changeBoxes) {
        this.changeBoxes = changeBoxes;
    }

    public List<Operation> getChangeMainAssetRfids() {
        return changeMainAssetRfids;
    }

    public void setChangeMainAssetRfids(List<Operation> changeMainAssetRfids) {
        this.changeMainAssetRfids = changeMainAssetRfids;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String code;
        private int userID;
        private List<Operation> boxesRfid;
        private List<Operation> changePersons;
        private List<Operation> changeBoxes;
        private List<Operation> changeMainAssetRfids;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder userID(int userID) {
            this.userID = userID;
            return this;
        }

        public Builder boxesRfid(List<Operation> boxesRfid) {
            this.boxesRfid = boxesRfid;
            return this;
        }

        public Builder changePersons(List<Operation> changePersons) {
            this.changePersons = changePersons;
            return this;
        }

        public Builder changeBoxes(List<Operation> changeBoxes) {
            this.changeBoxes = changeBoxes;
            return this;
        }

        public Builder changeMainAssetRfids(List<Operation> changeMainAssetRfids) {
            this.changeMainAssetRfids = changeMainAssetRfids;
            return this;
        }

        public UpLoadFile build() {
            return new UpLoadFile(code, userID, boxesRfid, changePersons, changeBoxes, changeMainAssetRfids);
        }

    }
}
