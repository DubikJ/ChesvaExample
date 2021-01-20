package ua.com.expertsolution.chesva.service.sync;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import ua.com.expertsolution.chesva.model.json.AuthResponse;
import ua.com.expertsolution.chesva.model.json.BoxRequest;
import ua.com.expertsolution.chesva.model.json.BoxResponse;
import ua.com.expertsolution.chesva.model.json.ChangeBoxRfidListRequest;
import ua.com.expertsolution.chesva.model.json.ChangeMainAssetRfidListRequest;
import ua.com.expertsolution.chesva.model.json.ConditionRequest;
import ua.com.expertsolution.chesva.model.json.ConditionResponse;
import ua.com.expertsolution.chesva.model.json.DownloadResponse;
import ua.com.expertsolution.chesva.model.json.MainAssetRequest;
import ua.com.expertsolution.chesva.model.json.MainAssetResponse;
import ua.com.expertsolution.chesva.model.json.PersonRequest;
import ua.com.expertsolution.chesva.model.json.PersonResponse;

import static ua.com.expertsolution.chesva.common.Consts.AUTHENTICATE_PATTERN_URL;
import static ua.com.expertsolution.chesva.common.Consts.CHANGE_BOX_RFID_LIST_PATTERN_URL;
import static ua.com.expertsolution.chesva.common.Consts.CHANGE_MAIN_ASSET_CHANGE_BOX_LIST_PATTERN_URL;
import static ua.com.expertsolution.chesva.common.Consts.CHANGE_MAIN_ASSET_CHANGE_PERSON_PATTERN_URL;
import static ua.com.expertsolution.chesva.common.Consts.CHANGE_MAIN_ASSET_RFID_LIST_PATTERN_URL;
import static ua.com.expertsolution.chesva.common.Consts.GET_BOX_PATTERN_URL;
import static ua.com.expertsolution.chesva.common.Consts.GET_CONDITION_PATTERN_URL;
import static ua.com.expertsolution.chesva.common.Consts.GET_MAIN_ASSETS_PATTERN_URL;
import static ua.com.expertsolution.chesva.common.Consts.GET_PERSON_PATTERN_URL;
import static ua.com.expertsolution.chesva.common.Consts.TOKEN_HEADER;

public interface SyncService {

    @FormUrlEncoded
    @POST(AUTHENTICATE_PATTERN_URL)
    Observable<AuthResponse> authenticate(@Field("grant_type") String grantType,
                                          @Field("username") String userName,
                                          @Field("password") String password);

    @POST(GET_BOX_PATTERN_URL)
    Observable<BoxResponse> getBoxList(@Header(TOKEN_HEADER) String token, @Body BoxRequest request);

    @POST(GET_MAIN_ASSETS_PATTERN_URL)
    Observable<MainAssetResponse> getMainAssetsList(@Header(TOKEN_HEADER) String token, @Body MainAssetRequest request);

    @POST(GET_CONDITION_PATTERN_URL)
    Observable<ConditionResponse> getConditionList(@Header(TOKEN_HEADER) String token, @Body ConditionRequest request);

    @POST(GET_PERSON_PATTERN_URL)
    Observable<PersonResponse> getPersonList(@Header(TOKEN_HEADER) String token, @Body PersonRequest request);

    @POST(CHANGE_BOX_RFID_LIST_PATTERN_URL)
    Observable<DownloadResponse> changeBoxRfidList(@Header(TOKEN_HEADER) String token, @Body ChangeBoxRfidListRequest request);

    @POST(CHANGE_MAIN_ASSET_RFID_LIST_PATTERN_URL)
    Observable<DownloadResponse> changeMainAssetRfidList(@Header(TOKEN_HEADER) String token, @Body ChangeMainAssetRfidListRequest request);

    @POST(CHANGE_MAIN_ASSET_CHANGE_BOX_LIST_PATTERN_URL)
    Observable<DownloadResponse> changeMainAssetChangeBoxList(@Header(TOKEN_HEADER) String token, @Body ChangeMainAssetRfidListRequest request);

    @POST(CHANGE_MAIN_ASSET_CHANGE_PERSON_PATTERN_URL)
    Observable<DownloadResponse> changeMainAssetChangePersonList(@Header(TOKEN_HEADER) String token, @Body ChangeMainAssetRfidListRequest request);

}
