package ua.com.expertsolution.chesva.scanner;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.zebra.adc.decoder.Barcode2DWithSoft;

import java.util.ArrayList;
import java.util.List;

import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.utils.DeviceUtils;

import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_BARCODE;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_RFID;

//import com.alien.barcode.BarcodeCallback;
//import com.alien.barcode.BarcodeReader;

public class BarcodeScanner {

    //    private BarcodeReader barcodeAlien;
    private Barcode2DWithSoft barcode2DWithSoft;
    private CallBackListener callBackListener;
    private Context context;
    private String nameDevice;
    private List<String> resultCode = new ArrayList<>();

    public BarcodeScanner(final Context context, final CallBackListener callBackListener) {
        this.callBackListener = callBackListener;
        this.nameDevice = DeviceUtils.getDeviceName();
        this.context = context;
        DevBeep.init(context);
        switch (nameDevice) {
            case Device.NAME_DEVICE_CHAINWAY:
                try {
                    barcode2DWithSoft= Barcode2DWithSoft.getInstance();
                } catch (Exception ex) {
                    Log.e(TAGLOG_RFID, "RFID init failed: " + ex.getMessage());
                    callBackListener.onErrorInit(context.getString(R.string.device_not_supported));
                    return;
                }

                if (barcode2DWithSoft != null) {
                    new AsyncTask<String, Integer, Boolean>() {
                        @Override
                        protected Boolean doInBackground(String... params) {
                            try {
                                return barcode2DWithSoft.open(context);
                            }catch (Exception e){
                                Log.e(TAGLOG_RFID, "RFID init failed: " + e.getMessage());
                                return false;
                            }

                        }

                        @Override
                        protected void onPostExecute(Boolean result) {
                            super.onPostExecute(result);
                            if (!result) {
                                callBackListener.onErrorInit(context.getString(R.string.rfid_load_failed));
                            }else{
//                barcode2DWithSoft.setParameter(324, 1);
//                barcode2DWithSoft.setParameter(300, 0); // Snapshot Aiming
//                barcode2DWithSoft.setParameter(361, 0); // Image Capture Illumination
                                barcode2DWithSoft.setParameter(6, 1);
                                barcode2DWithSoft.setParameter(22, 0);
                                barcode2DWithSoft.setParameter(23, 55);
                                barcode2DWithSoft.setParameter(402, 1);
                                callBackListener.onFinishInit();
                            }
                        }

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            callBackListener.onStartInit();
                        }
                    }.execute();
                }
                break;
            case Device.NAME_DEVICE_ALIEN:
//                try {
//                barcodeAlien = new BarcodeReader(context);
//                }catch (Exception e){
//                    callBackListener.onErrorInit(context.getString(R.string.device_not_supported));
//                }
                break;
//            case BARCODE_NEWLAND:
//                scannerManagerNewland = ScannerManager.getInstance();
//                scannerManagerNewland.setScanMode(ScannerManager.SCAN_SINGLE_MODE);
//                scannerManagerNewland.setDataTransferType(ScannerManager.TRANSFER_BY_EDITTEXT);
//                iScannerStatusListener = new ScannerManager.IScannerStatusListener() {
//                    @Override
//                    public void onScannerStatusChanage(int i) {
//
//                    }
//
//                    @Override
//                    public void onScannerResultChanage(byte[] bytes) {
//                        try {
//
//                            String result = new String(bytes, "UTF-8");
//
//                            if (!resultCode.contains(result)) {
//                                resultCode.add(result);
//                                DevBeep.PlayOK();
//                            }
//
////                            if (resultCode.size() == 1) {
////                                new Handler(Looper.getMainLooper()).post(new Runnable() {
////                                    @Override
////                                    public void run() {
////                                        if (callBackListener != null) {
////                                            callBackListener.onResultScan(resultCode.get(0));
////                                        }
////                                    }
////                                });
////
////                                if (scannerManagerNewland != null) {
////                                    scannerManagerNewland.removeScannerStatusListener(iScannerStatusListener);
////                                    scannerManagerNewland.stopContinuousScan();
////                                }
////                            }
//
//
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                };
//                break;

//            case BARCODE_CMC:
//                barcodeCMC = new Barcode(context);
//                barcodeCMC.setOnReceiveBarcode(new Barcode.OnReceiveBarcode() {
//
//                    public void onBarcodeData(byte[] data, int size) {
//                        final String barcoeString = new String(data, 0, size);
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (callBackListener != null) {
//                                    DevBeep.PlayOK();
//                                    callBackListener.onResultScan(barcoeString);
//                                }
//                            }
//                        });
//
//                    }
//                });
//                barcodeCMC.barcodePowerOn();
//                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        try {
//                                            barcodeCMC.open();
//                                        } catch (SecurityException e) {
//                                            e.printStackTrace();
//                                        } catch (IOException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                }, 500);


        }
    }

    public Boolean start(){
        resultCode.clear();
        switch (nameDevice) {
            case Device.NAME_DEVICE_CHAINWAY:
                if(barcode2DWithSoft!=null) {
                    barcode2DWithSoft.scan();
                    barcode2DWithSoft.setScanCallback((i, length, bytes) -> {
                        if (length < 1) {
                            if (length == -1) {
                                callBackListener.onError(context.getString(R.string.cancel_scan_barcode));
                            } else if (length == 0) {
                                callBackListener.onError(context.getString(R.string.time_out_scan_barcode));
                            } else {
                                callBackListener.onError(context.getString(R.string.error_scan_barcode));
                            }
                        }else{
                            try {
                                callBackListener.onResultScan(new String(bytes, 0, length, "ASCII"));
                            } catch (Exception ex)   {
                                callBackListener.onError(context.getString(R.string.error_scan_barcode));
                            }
                        }

                    });
                }
                return true;
            case Device.NAME_DEVICE_ALIEN:
//                barcodeAlien.start(new BarcodeCallback() {
//                    @Override
//                    public void onBarcodeRead(String s) {
//                        if(callBackListener!=null) {
//                            DevBeep.PlayOK();
//                            callBackListener.onResultScan(s);
//                        }
//                    }});
                return true;
//            case BARCODE_NEWLAND:
//                scannerManagerNewland.addScannerStatusListener(iScannerStatusListener);
//                scannerManagerNewland.startContinuousScan();
//                    return true;
//            case BARCODE_CMC:
//                barcodeCMC.scanBarcode();
//                return true;
        }
        return false;
    }

    public Boolean stop(){

        switch (nameDevice) {
            case Device.NAME_DEVICE_CHAINWAY:
                if (barcode2DWithSoft != null)
                    barcode2DWithSoft.stopScan();
                return true;
            case Device.NAME_DEVICE_ALIEN:
//                if (barcodeAlien != null)
//                    barcodeAlien.stop();
                return true;
//            case BARCODE_NEWLAND:
//                if (scannerManagerNewland != null) {
//                    scannerManagerNewland.removeScannerStatusListener(iScannerStatusListener);
//                    scannerManagerNewland.stopContinuousScan();
//                }
//                return true;
//            case BARCODE_CMC:
//                if (barcodeCMC != null) {
//                    barcodeCMC.barcodePowerOff();
//                    barcodeCMC.close();
//                }
//                return true;
        }
        return false;
    }

    public Boolean destroy(){
        callBackListener = null;
        switch (nameDevice) {
            case Device.NAME_DEVICE_CHAINWAY:
                if(barcode2DWithSoft!=null){
                    barcode2DWithSoft.stopScan();
                    barcode2DWithSoft.close();
                }
                return true;
            case Device.NAME_DEVICE_ALIEN:
//                if (barcodeAlien != null) {
//                    barcodeAlien.stop();
//                    barcodeAlien = null;
//                }
                return true;
//            case BARCODE_NEWLAND:
//                if (scannerManagerNewland != null) {
//                    scannerManagerNewland.stopContinuousScan();
//                }
//                return true;
//            case BARCODE_CMC:
//                if (barcodeCMC != null) {
//                    barcodeCMC.barcodePowerOff();
//                    barcodeCMC.close();
//                }
//                return true;
        }
        return false;
    }

    public interface CallBackListener{
        void onStartInit();
        void onFinishInit();
        void onErrorInit(String text);
        void onResultScan(String result);
        void onError(String text);
    }
}
