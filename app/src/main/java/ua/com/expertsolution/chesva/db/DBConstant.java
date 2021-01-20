package ua.com.expertsolution.chesva.db;

import ua.com.expertsolution.chesva.utils.StringUtils;

public interface DBConstant {


    String NAME_DATA_BASE_ROOM = "inventory_db";
    int DATA_BASE_VERSION = 16;

    //MainAssets
    String MAIN_ASSET_TABLE = "main_asset";
    String MAIN_ASSET_ID = "id";
    String MAIN_ASSET_NAME = "name";
    String MAIN_ASSET_NAME_UPPER = "name_upper";
    String MAIN_ASSET_MANUFACTURER_NAME = "manufacturer_name";
    String MAIN_ASSET_MODEL_NAME = "model_name";
    String MAIN_ASSET_MODEL_NAME_UPPER = "model_name_upper";
    String MAIN_ASSET_RFID = "rfid";
    String MAIN_ASSET_RFID_UPPER = "rfid_upper";
    String MAIN_ASSET_CONDITION_ID = "condition_id";
    String MAIN_ASSET_CONDITION_NAME = "condition_name";
    String MAIN_ASSET_CONDITION_NAME_UPPER = "condition_name_upper";
    String MAIN_ASSET_COMMENT = "comment";
    String MAIN_ASSET_PERSON_ID = "person_id";
    String MAIN_ASSET_PERSON_ID_OLD = "person_id_old";
    String MAIN_ASSET_PERSON_NAME = "person_name";
    String MAIN_ASSET_PERSON_NAME_UPPER = "person_name_upper";
    String MAIN_ASSET_BOX_ID = "box_id";
    String MAIN_ASSET_BOX_NAME = "box_name";
    String MAIN_ASSET_BOX_NAME_UPPER = "box_name_upper";

    //Box
    String BOX_TABLE = "box";
    String BOX_ID = "id";
    String BOX_NAME = "name";
    String BOX_NAME_UPPER = "name_upper";
    String BOX_RFID = "rfid";
    String BOX_RFID_UPPER = "rfid_upper";
    String BOX_LOCATION_ID = "location_id";
    String BOX_LOCATION_NAME = "location_name";
    String BOX_USER_ID = "user_id";
    String BOX_USER_NAME = "user_name";
    String BOX_TYPE_ID = "box_type_id";
    String BOX_TYPE_NAME = "box_type_name";

    //Condition
    String CONDITION_TABLE = "condition";
    String CONDITION_ID = "id";
    String CONDITION_NAME = "name";
    String CONDITION_NEED_COMMENT = "need_comment";

    //Person
    String PERSON_TABLE = "person";
    String PERSON_ID = "id";
    String PERSON_FIRST_NAME = "first_name";
    String PERSON_LAST_NAME = "last_name";
    String PERSON_FULL_NAME = "full_name";
    String PERSON_FULL_NAME_UPPER = "full_name_upper";
    String PERSON_PATRONYMIC = "patronymic";
    String PERSON_MOBILE_PHONE = "mobile_phone";
    String PERSON_EMAIL = "email";
    String PERSON_RFID = "rfid";
    String PERSON_RFID_UPPER = "rfid_upper";

    //Operation
    String OPERATION_TABLE = "operation";
    String OPERATION_ID = "id";
    String OPERATION_TYPE_OPERATION = "type_operation";
    String OPERATION_ID_OWNER = "id_owner";
    String OPERATION_RFID = "rfid";
    String OPERATION_EDITED = "edited";
    String OPERATION_TIME_EDIT_PERSON = "time_edit_person";
    String OPERATION_TEMP_ID = "temp_id";
    String OPERATION_SEND = "send";
    String OPERATION_CONDITION_ID = "condition_id";
    String OPERATION_COMMENT = "comment";
    String OPERATION_PERSON_ID = "person_id";
    String OPERATION_REPAIR_COMMENT = "repair_comment";
    String OPERATION_BOX_ID = "box_id";
    String OPERATION_OWNER_NAME = "owner_name";
    String OPERATION_PERSON_NAME = "person_name";
    String OPERATION_BOX_NAME = "box_name";
    String OPERATION_MODEL_NAME = "model_name";

}
