package ua.com.expertsolution.chesva.scanner;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class TagProximator {
    public static int RssiHistoryCapacity = 5;
    public static Double RssiMax = Double.valueOf(-30.0d);
    public static Double RssiMin = Double.valueOf(-70.0d);
    private static int scaledMax = 100;
    private static Hashtable tags = new Hashtable();

    public static float addData(String epc, double rssi) {
        Vector<Double> tagRSSIs = (Vector) tags.get(epc);
        if (tagRSSIs == null) {
            tagRSSIs = new Vector<>();
            tags.put(epc, tagRSSIs);
        }
        tagRSSIs.add(Double.valueOf(rssi));
        while (tagRSSIs.size() > RssiHistoryCapacity) {
            tagRSSIs.remove(0);
        }
        return getProximity(epc);
    }

    public static float getProximity(String tagID) {
        float rssiAvg = 0.0f;
        Vector<Double> tagRSSIs = (Vector) tags.get(tagID);
        if (tagRSSIs == null) {
            return 0.0f;
        }
        Iterator i = tagRSSIs.iterator();
        while (i.hasNext()) {
            rssiAvg = (float) (((double) rssiAvg) + ((Double) i.next()).doubleValue());
        }
        return rssiAvg / ((float) tagRSSIs.size());
    }

    public static int getScaledProximity(String epc, String deviceName) {
        return scaleRSSI(getProximity(epc), deviceName);
    }

    private static int scaleRSSI(float rssi, String deviceName) {
        if (((double) rssi) == 0.0d) {
            return 0;
        }
        switch (deviceName) {
            case Device.NAME_DEVICE_CHAINWAY:
                RssiMax = Double.valueOf(-49.0d);
                RssiMin = Double.valueOf(-71.0d);
                break;
            case Device.NAME_DEVICE_ALIEN:
                RssiMax = Double.valueOf(-30.0d);
                RssiMin = Double.valueOf(-70.0d);
                break;
        }

        return (int) (((((double) rssi) + (-RssiMin.doubleValue())) / (RssiMax.doubleValue() - RssiMin.doubleValue())) * ((double) scaledMax));
    }

    public static void clear() {
        tags.clear();
    }
}
