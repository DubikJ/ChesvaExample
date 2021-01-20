package ua.com.expertsolution.chesva.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ua.com.expertsolution.chesva.common.Consts.APP_SETTINGS_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.SPECIAL_WIFI_NETWORK_NAME;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG;
import static ua.com.expertsolution.chesva.common.Consts.USE_SPECIAL_WIFI_NETWORK;

public class NetworkUtils {

    public static boolean checkEthernet(Context context) {

        final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnected()) {
            if(SharedStorage.getBoolean(context, APP_SETTINGS_PREFS, USE_SPECIAL_WIFI_NETWORK, false)) {
                return checkWIFIConnectionToSpecialNetwork(context,
                        SharedStorage.getString(context, APP_SETTINGS_PREFS, SPECIAL_WIFI_NETWORK_NAME, ""))
                        == ConnectionStatus.CONNECTED;
            }else {
                return true;
            }
        } else {
            return false;
        }
    }

    public static boolean WIFISwitch(Context context) {

        final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkUSBconnection(Context context) {

        Intent intent = context.registerReceiver(null, new IntentFilter("android.hardware.usb.action.USB_STATE"));
        return intent.getExtras().getBoolean("connected");

    }

    public static boolean checkIsIP(String adress){
        Pattern p = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
        Matcher m = p.matcher(adress);
        return m.find();
    }


    public static boolean connectToSpecialWifi(Context mContext, String ssid, String pass, String type,
                                               OnConnectToWifiCallBackListener onConnectToWifiCallBackListener) {

        if (checkWIFIConnectionToSpecialNetwork(mContext, ssid) == ConnectionStatus.CONNECTED) {
            return true;
        }

        WifiManager wifiManager = (WifiManager) mContext
                .getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

//        WifiConfiguration wifiConfig = new WifiConfiguration();
//        wifiConfig.SSID = "\"" + ssid + "\"";
//
//        if (type.contains("WEP")) {
//            // wep
//            wifiConfig.wepKeys[0] = "\"" + pass + "\"";
//            wifiConfig.wepTxKeyIndex = 0;
//            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//        } else if (type.contains("WPA")) {
//            // wpa
//            wifiConfig.preSharedKey = "\"" + pass + "\"";
//        } else if (type.contains("OPEN")) {
//            // open
//            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//        }
//
//        wifiConfig.priority = 99999;
//
//        wifiManager.addNetwork(wifiConfig);
//
//        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
//        if(list!=null) {
//            for (WifiConfiguration i : list) {
//                if (i.SSID != null && i.SSID.equals(wifiConfig.SSID)) {
//                    wifiManager.disconnect();
//                    wifiManager.enableNetwork(i.networkId, true);
//                    wifiManager.reconnect();
//                    Log.d(TAGLOG_SYNC, "conneting to: ssid");
//                    IntentFilter intentFilter = new IntentFilter();
//                    intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//                    onConnectToWifiCallBackListener.onStartConnecting();
//                    BroadcastReceiver br = new BroadcastReceiver() {
//                        @Override
//                        public void onReceive(Context context, Intent intent) {
//                            final String action = intent.getAction();
//                            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
//                                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//                                if (info.isConnected()) {
//                                    onConnectToWifiCallBackListener.onConnected();
//                                }
//                            }
//                        }
//                    };
//                    mContext.registerReceiver(br, intentFilter);
//                    new Handler().postDelayed(() -> {
//                        mContext.unregisterReceiver(br);
//                        if (checkWIFIConnectionToSpecialNetwork(mContext, ssid) != ConnectionStatus.CONNECTED) {
//                            onConnectToWifiCallBackListener.onErrorConnect();
//                        }
//                    }, 20000);
//                    return false;
//                }
//            }
//        }

        WifiConfiguration conf = new WifiConfiguration();
        conf.hiddenSSID = true;
        conf.priority = 1000;
        conf.SSID = "\"" + ssid + "\"";
        conf.preSharedKey = "\"" + pass + "\"";
        conf.status = WifiConfiguration.Status.ENABLED;
        conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);

        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);

        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        int res = wifiManager.addNetwork(conf);
        boolean es = wifiManager.saveConfiguration();
        wifiManager.disconnect();
        boolean bRet = wifiManager.enableNetwork(res, true);
        wifiManager.reconnect();

        Log.d(TAGLOG, "conneting to: ssid"+ssid);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if(onConnectToWifiCallBackListener!=null) {
            onConnectToWifiCallBackListener.onStartConnecting();
            final boolean[] onCallBackCalled = {false};
            BroadcastReceiver br = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                        if (info.isConnected() && !onCallBackCalled[0]) {
                            onConnectToWifiCallBackListener.onConnected();
                            onCallBackCalled[0] = true;
                            mContext.unregisterReceiver(this);
                        }
                    }
                }
            };
            mContext.registerReceiver(br, intentFilter);
            new Handler().postDelayed(() -> {
                if (!onCallBackCalled[0]) {
                    mContext.unregisterReceiver(br);
                    if (checkWIFIConnectionToSpecialNetwork(mContext, ssid) != ConnectionStatus.CONNECTED) {
                        onConnectToWifiCallBackListener.onErrorConnect();
                    }
                }
            }, 10000);
        }
        return false;

    }

    private static ConnectionStatus checkWIFIConnectionToSpecialNetwork(Context mContext, String ssid) {

        if (!WIFISwitch(mContext)) {
            return ConnectionStatus.WIFI_OFF;
        }

        final WifiManager wifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo != null) {
            if(!wifiInfo.getSSID().equals("\"" + ssid + "\"")){
                return ConnectionStatus.NOT_CONNECTED_WIFI;
            }
        }


        return ConnectionStatus.CONNECTED;

    }

    private static class SocketConfig {
        String server;
        int port;
    }

    enum ConnectionStatus {
        CONNECTED,
        CONNECTION,
        NOT_CONNECTED,
        WIFI_OFF,
        NOT_CONNECTED_WIFI,
        ACCIDENT
    }

    public interface OnConnectToWifiCallBackListener{
        void onStartConnecting();
        void onConnected();
        void onErrorConnect();
    }

}
