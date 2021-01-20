package ua.com.expertsolution.chesva.model.db;


import android.text.TextUtils;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import ua.com.expertsolution.chesva.db.DBConstant;


@Parcel(Parcel.Serialization.BEAN)
public class Facility {

    private int id;
    private int state;
    private String name;
    private String nameUpper;
    private String rfid;
    private String rfidUpper;
    private String barcode;
    private String barcodeUpper;
    private String inventoryNumber;
    private String inventoryNumberUpper;
    private String serialNumber;
    private String serialNumberUpper;
    private Long dateInventory;
    private String field1;
    private String field2;
    private String field3;
    private String field4;
    private String field5;
    private String field6;
    private String field7;
    private String field8;
    private String field9;
    private String field10;
    private String field11;
    private String field12;

    @ParcelConstructor
    public Facility(int id, int state, String name, String nameUpper, String rfid, String rfidUpper, String barcode,
                    String barcodeUpper, String inventoryNumber, String inventoryNumberUpper, String serialNumber, String serialNumberUpper,
                    Long dateInventory, String field1, String field2, String field3, String field4, String field5,
                    String field6, String field7, String field8, String field9, String field10,
                    String field11, String field12) {
        this.id = id;
        this.state = state;
        this.name = name;
        this.nameUpper = nameUpper;
        this.rfid = rfid;
        this.rfidUpper = rfidUpper;
        this.barcode = barcode;
        this.barcodeUpper = barcodeUpper;
        this.inventoryNumber = inventoryNumber;
        this.inventoryNumberUpper = inventoryNumberUpper;
        this.serialNumber = serialNumber;
        this.serialNumberUpper = serialNumberUpper;
        this.dateInventory = dateInventory;
        this.field1 = field1;
        this.field2 = field2;
        this.field3 = field3;
        this.field4 = field4;
        this.field5 = field5;
        this.field6 = field6;
        this.field7 = field7;
        this.field8 = field8;
        this.field9 = field9;
        this.field10 = field10;
        this.field11 = field11;
        this.field12 = field12;
    }

    @Ignore
    public Facility(int state, String name, String rfid, String barcode,
                    String inventoryNumber, String serialNumber, Long dateInventory,
                    String field1, String field2, String field3, String field4, String field5,
                    String field6, String field7, String field8, String field9, String field10,
                    String field11, String field12) {
        this.state = state;
        this.name = name;
        this.nameUpper = TextUtils.isEmpty(name) ? "" : name.toUpperCase();
        this.rfid = rfid;
        this.rfidUpper = TextUtils.isEmpty(rfid) ? "" : rfid.toUpperCase();
        this.barcode = barcode;
        this.barcodeUpper = TextUtils.isEmpty(barcode) ? "" : barcode.toUpperCase();
        this.inventoryNumber = inventoryNumber;
        this.inventoryNumberUpper = TextUtils.isEmpty(inventoryNumber) ? "" : inventoryNumber.toUpperCase();
        this.serialNumber = serialNumber;
        this.serialNumberUpper = TextUtils.isEmpty(serialNumber) ? "" : serialNumber.toUpperCase();
        this.dateInventory = dateInventory;
        this.field1 = field1;
        this.field2 = field2;
        this.field3 = field3;
        this.field4 = field4;
        this.field5 = field5;
        this.field6 = field6;
        this.field7 = field7;
        this.field8 = field8;
        this.field9 = field9;
        this.field10 = field10;
        this.field11 = field11;
        this.field12 = field12;
    }

    @Ignore
    public Facility(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.nameUpper = name.toUpperCase();
    }

    public String getNameUpper() {
        return nameUpper;
    }

    public void setNameUpper(String nameUpper) {
        this.nameUpper = nameUpper;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
        this.rfidUpper = rfid.toUpperCase();
    }

    public String getRfidUpper() {
        return rfidUpper;
    }

    public void setRfidUpper(String rfidUpper) {
        this.rfidUpper = rfidUpper;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
        this.barcodeUpper = barcode.toUpperCase();
    }

    public String getBarcodeUpper() {
        return barcodeUpper;
    }

    public void setBarcodeUpper(String barcodeUpper) {
        this.barcodeUpper = barcodeUpper;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public void setInventoryNumber(String inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
        this.inventoryNumberUpper = inventoryNumber.toUpperCase();
    }

    public String getInventoryNumberUpper() {
        return inventoryNumberUpper;
    }

    public void setInventoryNumberUpper(String inventoryNumberUpper) {
        this.inventoryNumberUpper = inventoryNumberUpper;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        this.serialNumberUpper = serialNumber.toUpperCase();
    }

    public String getSerialNumberUpper() {
        return serialNumberUpper;
    }

    public void setSerialNumberUpper(String serialNumberUpper) {
        this.serialNumberUpper = serialNumberUpper;
    }

    public Long getDateInventory() {
        return dateInventory;
    }

    public void setDateInventory(Long dateInventory) {
        this.dateInventory = dateInventory;
    }

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public String getField3() {
        return field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    public String getField4() {
        return field4;
    }

    public void setField4(String field4) {
        this.field4 = field4;
    }

    public String getField5() {
        return field5;
    }

    public void setField5(String field5) {
        this.field5 = field5;
    }

    public String getField6() {
        return field6;
    }

    public void setField6(String field6) {
        this.field6 = field6;
    }

    public String getField7() {
        return field7;
    }

    public void setField7(String field7) {
        this.field7 = field7;
    }

    public String getField8() {
        return field8;
    }

    public void setField8(String field8) {
        this.field8 = field8;
    }

    public String getField9() {
        return field9;
    }

    public void setField9(String field9) {
        this.field9 = field9;
    }

    public String getField10() {
        return field10;
    }

    public void setField10(String field10) {
        this.field10 = field10;
    }

    public String getField11() {
        return field11;
    }

    public void setField11(String field11) {
        this.field11 = field11;
    }

    public String getField12() {
        return field12;
    }

    public void setField12(String field12) {
        this.field12 = field12;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int state;
        private String name;
        private String rfid;
        private String barcode;
        private String inventoryNumber;
        private String serialNumber;
        private Long dateInventory;
        private String field1;
        private String field2;
        private String field3;
        private String field4;
        private String field5;
        private String field6;
        private String field7;
        private String field8;
        private String field9;
        private String field10;
        private String field11;
        private String field12;

        public Builder state(int state) {
            this.state = state;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder rfid(String rfid) {
            this.rfid = rfid;
            return this;
        }

        public Builder barcode(String barcode) {
            this.barcode = barcode;
            return this;
        }

        public Builder inventoryNumber(String inventoryNumber) {
            this.inventoryNumber = inventoryNumber;
            return this;
        }

        public Builder serialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        public Builder dateInventory(Long dateInventory) {
            this.dateInventory = dateInventory;
            return this;
        }

        public Builder field1(String field1) {
            this.field1 = field1;
            return this;
        }

        public Builder field2(String field2) {
            this.field2 = field2;
            return this;
        }

        public Builder field3(String field3) {
            this.field3 = field3;
            return this;
        }

        public Builder field4(String field4) {
            this.field4 = field4;
            return this;
        }

        public Builder field5(String field5) {
            this.field5 = field5;
            return this;
        }

        public Builder field6(String field6) {
            this.field6 = field6;
            return this;
        }

        public Builder field7(String field7) {
            this.field7 = field7;
            return this;
        }

        public Builder field8(String field8) {
            this.field8 = field8;
            return this;
        }

        public Builder field9(String field9) {
            this.field9 = field9;
            return this;
        }

        public Builder field10(String field10) {
            this.field10 = field10;
            return this;
        }

        public Builder field11(String field11) {
            this.field11 = field11;
            return this;
        }

        public Builder field12(String field12) {
            this.field12 = field12;
            return this;
        }

        public Facility build() {
            return new Facility(state, name, rfid, barcode, inventoryNumber, serialNumber, dateInventory,
                    field1, field2, field3, field4, field5, field6, field7, field8, field9, field10,
                    field11, field12);
        }

    }
}
