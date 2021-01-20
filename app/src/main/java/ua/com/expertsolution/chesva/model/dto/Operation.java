package ua.com.expertsolution.chesva.model.dto;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import ua.com.expertsolution.chesva.db.DBConstant;

@Parcel(Parcel.Serialization.BEAN)
@Entity(tableName = DBConstant.OPERATION_TABLE,
        indices = {@Index(value = {DBConstant.OPERATION_ID}, unique = true)})
public class Operation {

    public static final int TYPE_OPERATION_BOX_ADD_RFID = 1;
    public static final int TYPE_OPERATION_MAIN_ASSET_ADD_RFID = 2 ;
    public static final int TYPE_OPERATION_ISSUING_MAIN_ASSET = 3;
    public static final int TYPE_OPERATION_RETURNING_MAIN_ASSET = 4;
    public static final int TYPE_OPERATION_MAIN_ASSET_IN_BOX = 5;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBConstant.OPERATION_ID)
    @SerializedName("Pos")
    private int id;

    @ColumnInfo(name = DBConstant.OPERATION_TYPE_OPERATION)
    private transient int typeOperation;

    @ColumnInfo(name = DBConstant.OPERATION_ID_OWNER)
    @SerializedName("ID")
    private int idOwner;

    @ColumnInfo(name = DBConstant.OPERATION_RFID)
    @SerializedName("Rfid")
    private String rfid;

    @ColumnInfo(name = DBConstant.OPERATION_EDITED)
    @SerializedName("Edited")
    private String edited;

    @ColumnInfo(name = DBConstant.OPERATION_TIME_EDIT_PERSON)
    private transient long timeEdit;

    @ColumnInfo(name = DBConstant.OPERATION_TEMP_ID)
    @SerializedName("TempID")
    private int tempId;

    @ColumnInfo(name = DBConstant.OPERATION_SEND)
    private transient int send;

    @ColumnInfo(name = DBConstant.OPERATION_CONDITION_ID)
    @SerializedName("ConditionID")
    private int conditionID;

    @ColumnInfo(name = DBConstant.OPERATION_COMMENT)
    @SerializedName("Comment")
    private String comment;

    @ColumnInfo(name = DBConstant.OPERATION_PERSON_ID)
    @SerializedName("PersonID")
    private int personID;

    @ColumnInfo(name = DBConstant.OPERATION_BOX_ID)
    @SerializedName("BoxID")
    private int boxID;

    @ColumnInfo(name = DBConstant.OPERATION_REPAIR_COMMENT)
    @SerializedName("RepairComment")
    private String repairComment;

    @ColumnInfo(name = DBConstant.OPERATION_OWNER_NAME)
    private transient String ownerName;

    @ColumnInfo(name = DBConstant.OPERATION_PERSON_NAME)
    private transient String personName;

    @ColumnInfo(name = DBConstant.OPERATION_BOX_NAME)
    private transient String boxName;

    @ColumnInfo(name = DBConstant.OPERATION_MODEL_NAME)
    private transient String modelName;

    @ParcelConstructor
    public Operation(int id, int typeOperation, int idOwner, String rfid, String edited, long timeEdit,
                     int tempId, int send, int conditionID, String comment, int personID, int boxID,
                     String repairComment, String ownerName, String personName, String boxName, String modelName) {
        this.id = id;
        this.typeOperation = typeOperation;
        this.idOwner = idOwner;
        this.rfid = rfid;
        this.edited = edited;
        this.timeEdit = timeEdit;
        this.tempId = tempId;
        this.send = send;
        this.conditionID = conditionID;
        this.comment = comment;
        this.personID = personID;
        this.boxID = boxID;
        this.repairComment = repairComment;
        this.ownerName = ownerName;
        this.personName = personName;
        this.boxName = boxName;
        this.modelName = modelName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTypeOperation() {
        return typeOperation;
    }

    public void setTypeOperation(int typeOperation) {
        this.typeOperation = typeOperation;
    }

    public int getIdOwner() {
        return idOwner;
    }

    public void setIdOwner(int idOwner) {
        this.idOwner = idOwner;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getEdited() {
        return edited;
    }

    public void setEdited(String edited) {
        this.edited = edited;
    }

    public long getTimeEdit() {
        return timeEdit;
    }

    public void setTimeEdit(long timeEdit) {
        this.timeEdit = timeEdit;
    }

    public int getTempId() {
        return tempId;
    }

    public void setTempId(int tempId) {
        this.tempId = tempId;
    }

    public int getSend() {
        return send;
    }

    public void setSend(int send) {
        this.send = send;
    }

    public int getConditionID() {
        return conditionID;
    }

    public void setConditionID(int conditionID) {
        this.conditionID = conditionID;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getPersonID() {
        return personID;
    }

    public void setPersonID(int personID) {
        this.personID = personID;
    }

    public String getRepairComment() {
        return repairComment;
    }

    public void setRepairComment(String repairComment) {
        this.repairComment = repairComment;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public int getBoxID() {
        return boxID;
    }

    public void setBoxID(int boxID) {
        this.boxID = boxID;
    }

    public String getBoxName() {
        return boxName;
    }

    public void setBoxName(String boxName) {
        this.boxName = boxName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int id;
        private int typeOperation;
        private int idOwner;
        private String rfid;
        private String edited;
        private transient long timeEdit;
        private int tempId;
        private transient int send;
        private int conditionID;
        private String comment;
        private int personID;
        private int boxID;
        private String repairComment;
        private String ownerName;
        private String personName;
        private String boxName;
        private String modelName;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder typeOperation(int typeOperation) {
            this.typeOperation = typeOperation;
            return this;
        }

        public Builder idOwner(int idOwner) {
            this.idOwner = idOwner;
            return this;
        }

        public Builder rfid(String rfid) {
            this.rfid = rfid;
            return this;
        }

        public Builder edited(String edited) {
            this.edited = edited;
            return this;
        }

        public Builder timeEdit(long timeEdit) {
            this.timeEdit = timeEdit;
            return this;
        }

        public Builder tempId(int tempId) {
            this.tempId = tempId;
            return this;
        }

        public Builder send(int send) {
            this.send = send;
            return this;
        }

        public Builder conditionID(int conditionID) {
            this.conditionID = conditionID;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder personID(int personID) {
            this.personID = personID;
            return this;
        }

        public Builder repairComment(String repairComment) {
            this.repairComment = repairComment;
            return this;
        }

        public Builder boxID(int boxID) {
            this.boxID = boxID;
            return this;
        }

        public Builder ownerName(String ownerName) {
            this.ownerName = ownerName;
            return this;
        }

        public Builder personName(String personName) {
            this.personName = personName;
            return this;
        }

        public Builder boxName(String boxName) {
            this.boxName = boxName;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Operation build() {
            return new Operation(id, typeOperation, idOwner, rfid, edited, timeEdit, tempId, send,
                    conditionID, comment, personID, boxID, repairComment, ownerName, personName, boxName, modelName);
        }

    }
}
