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
@Entity(tableName = DBConstant.CONDITION_TABLE,
        indices = {@Index(value = {DBConstant.CONDITION_ID}, unique = true)})
public class Condition {

    @PrimaryKey
    @ColumnInfo(name = DBConstant.CONDITION_ID)
    @SerializedName("ID")
    private int id;

    @ColumnInfo(name = DBConstant.CONDITION_NAME)
    @SerializedName("Name")
    private String name;

    @ColumnInfo(name = DBConstant.CONDITION_NEED_COMMENT)
    @SerializedName("NeedComment")
    private Boolean needComment;

    @ParcelConstructor
    public Condition(int id, String name, Boolean needComment) {
        this.id = id;
        this.name = name;
        this.needComment = needComment;
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
    }

    public Boolean getNeedComment() {
        return needComment;
    }

    public void setNeedComment(Boolean needComment) {
        this.needComment = needComment;
    }
}
