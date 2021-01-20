package ua.com.expertsolution.chesva.scanner;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.interfaces.IUHF;

import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.common.Consts;
import ua.com.expertsolution.chesva.utils.DeviceUtils;
import ua.com.expertsolution.chesva.utils.SharedStorage;

import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_RFID;
//import com.senter.iot.support.openapi.uhf.UhfD2;

public class UnfScanner {

    private String deviceName;
//    private UHF uhf;
//    private Battery battery;
    private Context context;
    private ScannerGetTagCallback scannerGetTagCallback;
    private static volatile boolean bGetEPC = false;
    private static volatile boolean startRfidScan = false;
//    private RFIDReader alienReader;
    public RFIDWithUHFUART chainwayReader;
    private String maskScan;

    public UnfScanner(final Context context, ScannerGetTagCallback scannerGetTagCallback) {
        this.scannerGetTagCallback = scannerGetTagCallback;
        this.context = context;
        this.deviceName = DeviceUtils.getDeviceName();
    }

    public void init(ScannerInitCallback scannerInitCallback){
        switch (deviceName){
            case Device.NAME_DEVICE_CHAINWAY:
                try {
                    chainwayReader = RFIDWithUHFUART.getInstance();
                } catch (Exception ex) {
                    Log.e(TAGLOG_RFID, "RFID init failed: " + ex.getMessage());
                    scannerInitCallback.onErrorInit(context.getString(R.string.rfid_load_failed));
                    return;
                }

                if (chainwayReader != null) {
                    new AsyncTask<String, Integer, Boolean>() {
                        @Override
                        protected Boolean doInBackground(String... params) {
                            try {
                                return chainwayReader.init();
                            }catch (Exception e){
                                Log.e(TAGLOG_RFID, "RFID init failed: " + e.getMessage());
                                return false;
                            }

                        }

                        @Override
                        protected void onPostExecute(Boolean result) {
                            super.onPostExecute(result);
                            if (!result) {
                                scannerInitCallback.onErrorInit(context.getString(R.string.rfid_load_failed));
                            }else{
                                SharedStorage.setInteger(context, Consts.APP_SETTINGS_PREFS, Consts.UHF_POWER_MAX, 30);
                                SharedStorage.setInteger(context, Consts.APP_SETTINGS_PREFS, Consts.UHF_POWER_MIN, 5);
                                try {
                                    SharedStorage.setInteger(context, Consts.APP_SETTINGS_PREFS, Consts.UHF_POWER, chainwayReader.getPower());
                                }catch (Exception e){}
                                scannerInitCallback.onFinishInit();
                                setTagFromSharedStorage();
                            }
                        }

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            scannerInitCallback.onStartInit();
                        }
                    }.execute();
                }

                break;
            case Device.NAME_DEVICE_ALIEN:
//                if (alienReader == null) {
//                    scannerInitCallback.onStartInit();
//                    try {
//                        alienReader = RFID.open();
//                        SharedStorage.setInteger(context, Consts.APP_SETTINGS_PREFS, Consts.UHF_POWER_MAX, alienReader.getMaxPower());
//                        SharedStorage.setInteger(context, Consts.APP_SETTINGS_PREFS, Consts.UHF_POWER_MIN, alienReader.getMinPower());
//                        Log.i(TAGLOG_RFID, "RFID init ");
//                        scannerInitCallback.onFinishInit();
//                    } catch (ReaderException e) {
//                        Log.e(TAGLOG_RFID, "RFID init failed: " + e);
//                        scannerInitCallback.onErrorInit(context.getString(R.string.rfid_load_failed));
//                    } catch (Exception e) {
//                        Log.e(TAGLOG_RFID, "RFID init failed: " + e);
//                        scannerInitCallback.onErrorInit(context.getString(R.string.rfid_load_failed));
//                    }
//                }
                break;
//            case Device.NAME_DEVICE_NEWLAND:
//                break;
//            case Device.NAME_DEVICE_CMC:
//                if(uhf==null) {
//                    uhf = new UHF();
//                    uhf.setDebug(false);
//                    try {
//                        uhf.on();
//                        uhf.open();
//                        uhf.setUhfPower(sPref.getInt(SettingsActivity.RFID_POWER, 280),300, 200);
//                        if (battery != null) {
//                            battery.run();
//                        }
//                        GetEpcThread getEpcThread = new GetEpcThread();
//                        getEpcThread.start();
//                    } catch (SecurityException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                        rfidInvertoryCallback.onErrorInit(context.getString(R.string.rfid_load_failed));
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                        rfidInvertoryCallback.onErrorInit(context.getString(R.string.rfid_load_failed));
//                    }
//                }
//                break;
//            case Device.NAME_DEVICE_SENTER_PAD_T:
////                if (!UhfD2.getInstance().init()) {
////                    rfidInvertoryCallback.onErrorInit(context.getString(R.string.rfid_load_failed));
////                }
//                break;
            default:
                scannerInitCallback.onErrorInit(context.getString(R.string.device_not_supported));
                break;
        }
    }


    public boolean setPowerRFID(int savedPwr){

        switch (deviceName) {
            case Device.NAME_DEVICE_CHAINWAY:
                if (chainwayReader!=null){
                    try{
                        Log.d(TAGLOG_RFID, "[Reader]\t Get power: " + savedPwr);
                        return chainwayReader.setPower(savedPwr);
                    }catch (Exception e){
                        Log.e(TAGLOG_RFID, "[Reader]\t Get power: " + e);
                        return false;
                    }
                }
            case Device.NAME_DEVICE_ALIEN:
//                if (alienReader != null) {
//                    Log.d(TAGLOG_RFID, "[Reader]\t Get power: " + savedPwr);
//
//                    try {
//                        int readerMinPower = alienReader.getMinPower();
//                        int readerMaxPower = alienReader.getMaxPower();
//
//                        if (savedPwr < readerMinPower) savedPwr = readerMinPower;
//                        if (savedPwr > readerMaxPower) savedPwr = readerMaxPower;
//
//                    } catch (ReaderException e) {
//                        e.printStackTrace();
//                        Log.e(TAGLOG_RFID, "[Reader]\t Get power: " + e);
//                        scannerGetTagCallback.onError(context.getString(R.string.rfid_load_failed));
//                        return false;
//                    }
//
//                    try {
//                        alienReader.setPower(savedPwr);
//                        Log.d(TAGLOG_RFID, "Set power: " + Integer.valueOf(savedPwr));
//                        return true;
//                    } catch (InvalidParamException e) {
//                        scannerGetTagCallback.onError(context.getString(R.string.rfid_load_failed));
//                        e.printStackTrace();
//                        Log.d(TAGLOG_RFID, e.toString());
//                        try {
//                            alienReader.stop();
//                            alienReader = null;
//                        } catch (ReaderException e1) {
//                            e1.printStackTrace();
//                        }
//                        return false;
//                    } catch (ReaderException e) {
//                        scannerGetTagCallback.onError(context.getString(R.string.rfid_load_failed));
//                        e.printStackTrace();
//                        Log.d(TAGLOG_RFID, e.toString());
//                        try {
//                            alienReader.stop();
//                            alienReader = null;
//                        } catch (ReaderException e1) {
//                            e1.printStackTrace();
//                        }
                        return false;
//                    }
//                }
//            case Device.NAME_DEVICE_SENTER_PAD_T:
//
//                int savedPwr = SharedStorage.getInteger(context, APP_SETTINGS_PREFS, UHF_POWER, 30);
//                Log.d(TAGLOG_RFID, "[Reader]\t Get power: " + savedPwr);
//
//                if (savedPwr < 0) savedPwr = 0;
//                if (savedPwr > 32) savedPwr = 32;
////                UhfD2.getInstance().setOutputPower(savedPwr);
//                break;
        }
        return false;
    }

    public Boolean setMaskScan(String maskScan) {
        switch (deviceName) {
            case Device.NAME_DEVICE_CHAINWAY:
                if(chainwayReader==null){
                    return false;
                }
                if(maskScan == null){
                    setTagFromSharedStorage();
                    return chainwayReader.setFilter(IUHF.Bank_EPC, 32, 0, "");
                }
                if(chainwayReader.setFilter(IUHF.Bank_EPC, 32, 96, maskScan)){
                    return true;
                }else if(chainwayReader.setFilter(IUHF.Bank_EPC, 32, maskScan.length(), maskScan)){
                    return true;
                }else if(chainwayReader.setFilter(IUHF.Bank_EPC, 32, 16, maskScan)){
                    return true;
                }else if(chainwayReader.setFilter(IUHF.Bank_EPC, 32, 32, maskScan)){
                    return true;
                }else if(chainwayReader.setFilter(IUHF.Bank_EPC, 32, 48, maskScan)){
                    return true;
                }else if(chainwayReader.setFilter(IUHF.Bank_EPC, 32, 64, maskScan)){
                    return true;
                }else if(chainwayReader.setFilter(IUHF.Bank_EPC, 32, 80, maskScan)){
                    return true;
                }else{
                    return false;
                }
            default:
                this.maskScan = maskScan;
                return true;
        }
    }

    public boolean setTagFromSharedStorage() {
        switch (deviceName) {
            case Device.NAME_DEVICE_CHAINWAY:
                return setTagStandard(0x04);//SharedStorage.getInteger(context, APP_SETTINGS_PREFS, RFID_STANDARD, 0x04));
            default:
                return false;
        }
    }

    public boolean setTagStandard(int standard) {
        switch (deviceName) {
            case Device.NAME_DEVICE_CHAINWAY:
                return chainwayReader.setFrequencyMode((byte) standard);
            default:
                return false;
        }
    }

    public int getTagStandard() {
        switch (deviceName) {
            case Device.NAME_DEVICE_CHAINWAY:
                return chainwayReader.getFrequencyMode();
            default:
                return -1;
        }
    }

    public void startRfidScan() {
        if(deviceName.equalsIgnoreCase(Device.NAME_DEVICE_NEWLAND)){
            return;
        }
        if(startRfidScan){
            return;
        }
        startRfidScan = true;

        switch (deviceName) {
            case Device.NAME_DEVICE_CHAINWAY:
                if(chainwayReader!=null) {
                    try {
                    if (chainwayReader.startInventoryTag()) {
                        bGetEPC = true;
                        new GetEpcThread().start();
                    } else {
                        chainwayReader.stopInventory();
                    }
                    }catch (Exception e){
                        Log.e(TAGLOG_RFID, "ERROR: " + e);
                        scannerGetTagCallback.onError(context.getString(R.string.rfid_load_failed));
                    }
                }
                break;
            case Device.NAME_DEVICE_ALIEN:
//                if (alienReader == null || alienReader.isRunning()) return;
//
//                try {
//                    RFIDCallback rfidCallback = tag -> {
//                        Log.i(TAGLOG_RFID, "RFID" + tag.getEPC());
//                        if (scannerGetTagCallback != null) {
//                            scannerGetTagCallback.onTagRead(tag.getEPC(), String.valueOf(tag.getRSSI()));
//                        }
//                    };
//
//                    if(!TextUtils.isEmpty(maskScan)) {
////                        new Mask(Bank.EPC, 32, labelNum.length() * 4, labelNum)
//                        alienReader.inventory(rfidCallback, Mask.maskEPC(maskScan));
//                    }else{
//                        alienReader.inventory(rfidCallback);
//                    }
//
//                } catch (ReaderException e) {
//                    Log.e(TAGLOG_RFID, "ERROR: " + e);
//                    scannerGetTagCallback.onError(context.getString(R.string.rfid_load_failed));
//                }
                break;
//            case Device.NAME_DEVICE_NEWLAND:
//                break;
//            case Device.NAME_DEVICE_CMC:
//                if(uhf!=null) {
//                    try {
//                        uhf.inventory();
//                        GetEpcThread getEpcThread = new GetEpcThread();
//                        getEpcThread.start();
//                    }catch (Exception e){
//                        Log.e(TAGLOG_RFID, "ERROR: " + e);
//                        rfidInvertoryCallback.onError(context.getString(R.string.rfid_load_failed));
//                    }
//                }
//                break;
//            case Device.NAME_DEVICE_SENTER_PAD_T:
//                startInventorySenderPadT();
//                break;
        }
    }

    public void startInventorySenderPadT() {
//        UhfD2.getInstance().iso18k6cRealTimeInventory(1, new UhfD2.UmdOnIso18k6cRealTimeInventory() {
//            @Override
//            public void onFinishedWithError(UhfD2.UmdErrorCode error) {
//                onFinishedOnce();
//            }
//
//            @Override
//            public void onFinishedSuccessfully(Integer antennaId, int readRate, int totalRead) {
//                onFinishedOnce();
//            }
//
//            private void onFinishedOnce() {
//                if (startRfidScan) {
//                    startInventorySenderPadT();
//                }
//            }
//
//            @Override
//            public void onTagInventory(UhfD2.UII uii, UhfD2.UmdFrequencyPoint frequencyPoint, Integer antennaId, UhfD2.UmdRssi rssi) {
//                rfidInvertoryCallback.onTagRead(DataTransfer.xGetString(uii.getBytes()));
//            }
//        });
    }

    public void stopRfidScan() {
        startRfidScan = false;
        switch (deviceName) {
            case Device.NAME_DEVICE_CHAINWAY:
                if (bGetEPC) {
                    bGetEPC = false;
                    try {
                        if (chainwayReader != null) {
                            chainwayReader.stopInventory();
                        }
                    } catch (Exception e) {
                        Log.e(TAGLOG_RFID, "ERROR: " + e);
                        scannerGetTagCallback.onError(context.getString(R.string.rfid_load_failed));
                    }
                }
                break;
            case Device.NAME_DEVICE_ALIEN:
//                if (alienReader != null && alienReader.isRunning()) {
//                    try {
//                        alienReader.stop();
//                    } catch (ReaderException e) {
//                        Log.e(TAGLOG_RFID, "ERROR: " + e);
//                        scannerGetTagCallback.onError(context.getString(R.string.rfid_load_failed));
//                    }
//                }
                break;
//            case Device.NAME_DEVICE_NEWLAND:
//                break;
//            case Device.NAME_DEVICE_CMC:
//                if(uhf!=null){
//            bGetEPC = false;
//                    try {
//                        uhf.stopInventory();
//                    }catch (Exception e) {
//                        Log.e(TAGLOG_RFID, "ERROR: " + e);
//                        rfidInvertoryCallback.onError(context.getString(R.string.rfid_load_failed));
//                    }
//                }
//                break;
        }
    }

    public void destroy() {
        switch (deviceName) {
            case Device.NAME_DEVICE_CHAINWAY:
                bGetEPC = false;
                if (chainwayReader != null) {
                    chainwayReader.free();
                    chainwayReader = null;
                }
                break;
            case Device.NAME_DEVICE_ALIEN:
//                if (alienReader != null) {
//                    alienReader.close();
//                    alienReader = null;
//                }
                break;
//            case Device.NAME_DEVICE_NEWLAND:
//                break;
//            case Device.NAME_DEVICE_CMC:
//            bGetEPC = false;
//                if(uhf!=null) {
//                    uhf.off();
//                    uhf.close();
//                }
//                if (battery != null) {
//                    battery.cancel();
//                }
//                break;
//            case Device.NAME_DEVICE_SENTER_PAD_T:
////                UhfD2.getInstance().iso18k6cSetAccessEpcMatch(UhfD2.UmdEpcMatchSetting.newInstanceOfDisable());
//                break;
        }
//        if (bGetEPC) {
//            bGetEPC = false;
//        }
        Log.d(TAGLOG_RFID, "RFID close");
    }

    class GetEpcThread extends Thread {

        public void run() {
            while (bGetEPC) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    switch (deviceName) {
                        case Device.NAME_DEVICE_CHAINWAY:
                            UHFTAGInfo tag = chainwayReader.readTagFromBuffer();
                            Log.d(TAGLOG_RFID, "readTagFromBuffer: "+tag);
                            if (tag != null) {
                                if (scannerGetTagCallback != null) {
                                    scannerGetTagCallback.onTagRead(tag.getEPC(), tag.getRssi());
                                }
                            }
                            break;
//                        case Device.NAME_DEVICE_CMC:
//
//                            int cnt = uhf.getSize();
//                            for (int i = 0; i < cnt; i++) {
//                                UHFTag uhfTag = uhf.getItem();
//
//                                if (uhfTag == null) {
//                                    continue;
//                                }
//                                if (rfidInvertoryCallback != null) {
//                                    rfidInvertoryCallback.onTagRead(uhfTag.getEpc(true).replaceAll(" ", ""));
//                                }
//                            }
//                            break;
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        }
    }


    public interface ScannerGetTagCallback {
        void onTagRead(String epc, String rssi);
        void onError(String text);
    }

    public interface ScannerInitCallback {
        void onStartInit();
        void onFinishInit();
        void onErrorInit(String text);
    }
}
