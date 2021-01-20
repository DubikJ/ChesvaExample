package ua.com.expertsolution.chesva.model.dto;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import ua.com.expertsolution.chesva.db.DBConstant;

@Parcel(Parcel.Serialization.BEAN)
@Entity(tableName = DBConstant.MAIN_ASSET_TABLE,
        indices = {@Index(value = {DBConstant.MAIN_ASSET_ID}, unique = true)})
public class MainAsset {

    @PrimaryKey
    @ColumnInfo(name = DBConstant.MAIN_ASSET_ID)
    @SerializedName("ID")
    private int id;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_NAME)
    @SerializedName("Name")
    private String name;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_NAME_UPPER)
    private transient String nameUpper;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_MANUFACTURER_NAME)
    @SerializedName("ManufacturerName")
    private String manufacturerName;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_MODEL_NAME)
    @SerializedName("ModelName")
    private String modelName;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_MODEL_NAME_UPPER)
    private transient String modelNameUpper;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_RFID)
    @SerializedName("Rfid")
    private String rfid;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_RFID_UPPER)
    private transient String rfidUpper;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_CONDITION_ID)
    @SerializedName("ConditionID")
    private int conditionID;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_CONDITION_NAME)
    @SerializedName("ConditionName")
    private String conditionName;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_CONDITION_NAME_UPPER)
    private transient String conditionNameUpper;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_COMMENT)
    @SerializedName("Comment")
    private String comment;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_PERSON_ID)
    @SerializedName("PersonID")
    private int personID;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_PERSON_NAME)
    @SerializedName("PersonName")
    private String personName;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_PERSON_NAME_UPPER)
    private transient String personNameUpper;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_BOX_ID)
    @SerializedName("BoxID")
    private int boxID;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_BOX_NAME)
    @SerializedName("BoxName")
    private String boxName;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_BOX_NAME_UPPER)
    private transient String boxNameUpper;

    @ColumnInfo(name = DBConstant.MAIN_ASSET_PERSON_ID_OLD)
    private transient int personIDOld;

    @ColumnInfo(name = DBConstant.OPERATION_TIME_EDIT_PERSON)
    private transient long timeEditPerson;

    @ParcelConstructor
    public MainAsset(int id, String name, String manufacturerName, String modelName, String rfid,
                     int conditionID, String conditionName, String comment, int personID,
                     String personName, int boxID, String boxName, int personIDOld, long timeEditPerson) {
        this.id = id;
        this.name = name;
        this.nameUpper = TextUtils.isEmpty(name) ? name : name.toUpperCase();
        this.manufacturerName = manufacturerName;
        this.modelName = modelName;
        this.modelNameUpper = TextUtils.isEmpty(modelName) ? modelName : modelName.toUpperCase();
        this.rfid = rfid;
        this.rfidUpper = TextUtils.isEmpty(rfid) ? rfid : rfid.toUpperCase();
        this.conditionID = conditionID;
        this.conditionName = conditionName;
        this.conditionNameUpper = TextUtils.isEmpty(conditionName) ? conditionName : conditionName.toUpperCase();
        this.comment = comment;
        this.personID = personID;
        this.personName = personName;
        this.personNameUpper = TextUtils.isEmpty(personName) ? personName : personName.toUpperCase();
        this.boxID = boxID;
        this.boxName = boxName;
        this.boxNameUpper = TextUtils.isEmpty(boxName) ? boxName : boxName.toUpperCase();
        this.personIDOld = personIDOld;
        this.timeEditPerson = timeEditPerson;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.nameUpper =  TextUtils.isEmpty(name) ? name : name.toUpperCase();
    }

    public String getNameUpper() {
        return TextUtils.isEmpty(name) ? name : name.toUpperCase();
    }

    public void setNameUpper(String nameUpper) {
        this.nameUpper = nameUpper;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
        this.modelNameUpper =  TextUtils.isEmpty(modelName) ? modelName : modelName.toUpperCase();
    }

    public String getModelNameUpper() {
        return TextUtils.isEmpty(modelName) ? modelName : modelName.toUpperCase();
    }

    public void setModelNameUpper(String rfidUpper) {
        this.rfidUpper = rfidUpper;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
        this.rfidUpper =  TextUtils.isEmpty(rfid) ? rfid : rfid.toUpperCase();
    }

    public String getRfidUpper() {
        return TextUtils.isEmpty(rfid) ? rfid : rfid.toUpperCase();
    }

    public void setRfidUpper(String rfidUpper) {
        this.rfidUpper = rfidUpper;
    }

    public int getConditionID() {
        return conditionID;
    }

    public void setConditionID(int conditionID) {
        this.conditionID = conditionID;
    }

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public String getConditionNameUpper() {
        return TextUtils.isEmpty(conditionName) ? conditionName : conditionName.toUpperCase();
    }

    public void setConditionNameUpper(String conditionNameUpper) {
        this.conditionNameUpper = conditionNameUpper;
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

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonNameUpper() {
        return TextUtils.isEmpty(personName) ? personName : personName.toUpperCase();
    }

    public void setPersonNameUpper(String personNameUpper) {
        this.personNameUpper = personNameUpper;
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

    public String getBoxNameUpper() {
        return TextUtils.isEmpty(boxName) ? boxName : boxName.toUpperCase();
    }

    public void setBoxNameUpper(String boxNameUpper) {
        this.boxNameUpper = boxNameUpper;
    }

    public int getPersonIDOld() {
        return personIDOld;
    }

    public void setPersonIDOld(int personIDOld) {
        this.personIDOld = personIDOld;
    }

    public long getTimeEditPerson() {
        return timeEditPerson;
    }

    public void setTimeEditPerson(long timeEditPerson) {
        this.timeEditPerson = timeEditPerson;
    }
}
