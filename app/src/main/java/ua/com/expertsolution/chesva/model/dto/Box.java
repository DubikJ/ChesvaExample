package ua.com.expertsolution.chesva.model.dto;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import ua.com.expertsolution.chesva.db.DBConstant;

import static ua.com.expertsolution.chesva.common.Consts.DATE_SYNC_FORMAT;

@Parcel(Parcel.Serialization.BEAN)
@Entity(tableName = DBConstant.BOX_TABLE,
        indices = {@Index(value = {DBConstant.BOX_ID}, unique = true)})
public class Box {

    @PrimaryKey
    @ColumnInfo(name = DBConstant.BOX_ID)
    @SerializedName("ID")
    private int id;

    @ColumnInfo(name = DBConstant.BOX_NAME)
    @SerializedName("Name")
    private String name;

    @ColumnInfo(name = DBConstant.BOX_NAME_UPPER)
    private transient String nameUpper;

    @ColumnInfo(name = DBConstant.BOX_RFID)
    @SerializedName("Rfid")
    private String rfid;

    @ColumnInfo(name = DBConstant.BOX_RFID_UPPER)
    private transient String rfidUpper;

    @ColumnInfo(name = DBConstant.BOX_LOCATION_ID)
    @SerializedName("LocationID")
    private int locationID;

    @ColumnInfo(name = DBConstant.BOX_LOCATION_NAME)
    @SerializedName("LocationName")
    private String locationName;

    @ColumnInfo(name = DBConstant.BOX_USER_ID)
    @SerializedName("UserID")
    private int userID;

    @ColumnInfo(name = DBConstant.BOX_USER_NAME)
    @SerializedName("UserName")
    private String userName;

    @ColumnInfo(name = DBConstant.BOX_TYPE_ID)
    @SerializedName("BoxTypeID")
    private int boxTypeID;

    @ColumnInfo(name = DBConstant.BOX_TYPE_NAME)
    @SerializedName("BoxTypeName")
    private String boxTypeName;

    @ParcelConstructor
    public Box(int id, String name, String rfid, int locationID, String locationName, int userID,
               String userName, int boxTypeID, String boxTypeName) {
        this.id = id;
        this.name = name;
        this.nameUpper = TextUtils.isEmpty(name) ? name : name.toUpperCase();
        this.rfid = rfid;
        this.rfidUpper = TextUtils.isEmpty(rfid) ? rfid : rfid.toUpperCase();
        this.locationID = locationID;
        this.locationName = locationName;
        this.userID = userID;
        this.userName = userName;
        this.boxTypeID = boxTypeID;
        this.boxTypeName = boxTypeName;
    }

    public int getId() {
        return id;
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

    public int getLocationID() {
        return locationID;
    }

    public String getLocationName() {
        return locationName;
    }

    public int getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public int getBoxTypeID() {
        return boxTypeID;
    }

    public String getBoxTypeName() {
        return boxTypeName;
    }
}
