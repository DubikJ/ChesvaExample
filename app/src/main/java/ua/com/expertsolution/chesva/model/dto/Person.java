package ua.com.expertsolution.chesva.model.dto;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import ua.com.expertsolution.chesva.db.DBConstant;

@Parcel(Parcel.Serialization.BEAN)
@Entity(tableName = DBConstant.PERSON_TABLE,
        indices = {@Index(value = {DBConstant.PERSON_ID}, unique = true)})
public class Person {

    @PrimaryKey
    @ColumnInfo(name = DBConstant.PERSON_ID)
    @SerializedName("ID")
    private int id;

    @ColumnInfo(name = DBConstant.PERSON_FIRST_NAME)
    @SerializedName("FirstName")
    private String firstName;

    @ColumnInfo(name = DBConstant.PERSON_LAST_NAME)
    @SerializedName("LastName")
    private String lastName;

    @ColumnInfo(name = DBConstant.PERSON_FULL_NAME)
    private transient String fullName;

    @ColumnInfo(name = DBConstant.PERSON_FULL_NAME_UPPER)
    private transient String fullNameUpper;

    @ColumnInfo(name = DBConstant.PERSON_PATRONYMIC)
    @SerializedName("Patronymic")
    private String patronymic;

    @ColumnInfo(name = DBConstant.PERSON_MOBILE_PHONE)
    @SerializedName("MobilePhone")
    private String mobilePhone;

    @ColumnInfo(name = DBConstant.PERSON_EMAIL)
    @SerializedName("Email")
    private String email;

    @ColumnInfo(name = DBConstant.PERSON_RFID)
    @SerializedName("Rfid")
    private String rfid;

    @ColumnInfo(name = DBConstant.PERSON_RFID_UPPER)
    private transient String rfidUpper;

    @Ignore
    private transient String issuedMainAsset;

    @ParcelConstructor
    public Person(int id, String firstName, String lastName, String patronymic,
                  String mobilePhone, String email, String rfid) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = (TextUtils.isEmpty(firstName)? "" : firstName + " ")+ lastName;
        this.fullNameUpper = (TextUtils.isEmpty(firstName)? "" : firstName.toUpperCase() + " ")+(TextUtils.isEmpty(lastName)? "" : lastName.toUpperCase());
        this.patronymic = patronymic;
        this.mobilePhone = mobilePhone;
        this.email = email;
        this.rfid = rfid;
        this.rfidUpper = TextUtils.isEmpty(rfid) ? rfid : rfid.toUpperCase();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.fullName = (TextUtils.isEmpty(firstName)? "" : firstName + " ")+ lastName;
        this.fullNameUpper = (TextUtils.isEmpty(firstName)? "" : firstName.toUpperCase() + " ")
                +(TextUtils.isEmpty(lastName)? "" : lastName.toUpperCase());
    }

    public String getFullName() {
        return (TextUtils.isEmpty(firstName)? "" : firstName + " ")+ lastName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullNameUpper() {
        return (TextUtils.isEmpty(firstName)? "" : firstName.toUpperCase() + " ")+(TextUtils.isEmpty(lastName)? "" : lastName.toUpperCase());
    }

    public void setFullNameUpper(String fullNameUpper) {
        this.fullNameUpper = fullNameUpper;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getIssuedMainAsset() {
        return issuedMainAsset;
    }

    public void setIssuedMainAsset(String issuedMainAsset) {
        this.issuedMainAsset = issuedMainAsset;
    }
}
