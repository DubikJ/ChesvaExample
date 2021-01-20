package ua.com.expertsolution.chesva.utils;

import android.text.TextUtils;

import java.text.DecimalFormat;
import java.util.UUID;

public class NumberUtils {

    private static DecimalFormat precisionDouble = new DecimalFormat("###,###,##0.00");
    private static DecimalFormat precisionInt = new DecimalFormat("###,###,###");

    public static Double stringToDouble(String number){
        if(TextUtils.isEmpty(number)) return 0.0;
        try{
            return Double.parseDouble(number.replace(",","."));
        }catch (NumberFormatException e){
            return 0.0;
        }
    }

    public static String doubleToString(Double number, boolean showFractions){
        if(showFractions && (number % 1 == 0))  return String.valueOf(number.intValue());

        if(number==null) return String.format("%.2f", 0.0).replace(".",",");
        try{
            return precisionDouble.format(number)
                    .replace(".",",");
        }catch (NumberFormatException e){
            return "0,0";
        }
    }

    public static String intToString(int number){
        try{
            return precisionInt.format(number);
        }catch (NumberFormatException e){
            return "0,0";
        }
    }

    public static String doubleFormatToString(String number){
        return doubleToString(stringToDouble(number), false);
    }

    public static Double doubleFormat(Double number){
        if(number==null) return 0.0;
        try{
            return Double.valueOf(String.format("%.2f", number).replaceAll(",","."));
        }catch (NumberFormatException e){
            return 0.0;
        }
    }

    public static int generateUniqueId() {
        UUID idOne = UUID.randomUUID();
        String str=""+idOne;
        int uid=str.hashCode();
        String filterStr=""+uid;
        str=filterStr.replaceAll("-", "");
        return Integer.parseInt(str);
    }

}
