package ua.com.expertsolution.chesva.common;

import java.text.SimpleDateFormat;

public final class Consts {

    public final static String TAGLOG = "Chesva";
    public final static String TAGLOG_RFID = "Chesva_RFID";
    public final static String TAGLOG_DOWNLOAD = "Chesva_Download";
    public final static String TAGLOG_UPLOAD = "Chesva_Upload";
    public final static String TAGLOG_LOAD = "Chesva_Load";
    public final static String TAGLOG_INVENTORY = "Chesva_Inv";
    public final static String TAGLOG_SYNC = "Chesva_Sync";
    public final static String TAGLOG_DB = "Chesva_DB";
    public final static String TAGLOG_BARCODE = "Chesva_BARCODE";

    public static final String APP_SETTINGS_PREFS = "chesva_prefs";
    public static final String APP_CASH_TOKEN_PREFS = "chesva_token_prefs";
    public final static String CHECK_PASSWORD = "987654321";

    // Settings
    public final static int CONNECT_TIMEOUT_SECONDS_RETROFIT = 20;
    public final static int CHECK_SERVER_TIMEOUT_SECONDS_RETROFIT = 20;
    public final static String TYPE_CONNECTION_HTTP = "http";
    public final static String TYPE_CONNECTION_HTTPS = "https";
    public final static String CONNECT_SERVER_URL = "localhost";

    public final static String TOKEN_HEADER = "Authorization";

    public final static String AUTHENTICATE_PATTERN_URL = "api/token";
    public final static String GET_BOX_PATTERN_URL = "api/Box/List";
    public final static String GET_MAIN_ASSETS_PATTERN_URL = "api/MainAsset/List";
    public final static String GET_CONDITION_PATTERN_URL = "api/Condition/List";
    public final static String GET_PERSON_PATTERN_URL = "api/Person/List";
    public final static String CHANGE_BOX_RFID_LIST_PATTERN_URL = "api/Box/ChangeRfidList";
    public final static String CHANGE_MAIN_ASSET_RFID_LIST_PATTERN_URL = "api/MainAsset/ChangeRfidList";
    public final static String CHANGE_MAIN_ASSET_CHANGE_BOX_LIST_PATTERN_URL = "api/MainAsset/ChangeBox";
    public final static String CHANGE_MAIN_ASSET_CHANGE_PERSON_PATTERN_URL = "api/MainAsset/ChangePersonList";

    public final static String USE_SPECIAL_WIFI_NETWORK = "_use_special_wifi_network";
    public final static String SPECIAL_WIFI_NETWORK_NAME = "_special_wifi_network_name";
    public static final String PERIOD_LICENSE = "_period_license";
    public static final String UHF_POWER = "_UHFPower";
    public static final String UHF_POWER_MAX = "_UHFPowerMax";
    public static final String UHF_POWER_MIN = "_UHFPowerMin";
    public static final String IS_DB_NO_EMPTY = "_db_no_empty";
    public final static String UI_LANG = "_ui_lang";
    public static final String SERVER = "_server";
    public static final String ENTERED_LOGIN = "_entered_login";
    public static final String ENTERED_PASSWORD = "_entered_password";
    public static final String RFID_STANDARD = "_rfid_standard";
    public final static String TYPE_CONNECTION = "_type_connection";


    public final static String VALID_DATE = "_valid_date";
    public final static String TOKEN = "_token";

    public static final int STATE_IN_SEARCH_OF = 1;     // У пошуку
    public static final int STATE_FOUND = 2;            // Знайдено
    public static final int STATE_CONFIRMED = 3;         // Підтверджено
    public static final int STATE_EXCESSIVE = 4;        // Зайве
    public static final int STATE_MISSING = 5;          // Помилка
    public static final int STATE_CREATED = 7;          // Создано


    public final static SimpleDateFormat DATE_SYNC_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    public final static SimpleDateFormat DATE_DAY_FORMAT = new SimpleDateFormat("dd_MM_yyyy");
    public final static SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd MM yyyy HH:mm");

}
