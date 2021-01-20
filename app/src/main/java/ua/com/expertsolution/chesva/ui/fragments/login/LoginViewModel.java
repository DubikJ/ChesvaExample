package ua.com.expertsolution.chesva.ui.fragments.login;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.io.IOException;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.HttpException;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.app.InventoryApplication;
import ua.com.expertsolution.chesva.model.LoadEventHandler;
import ua.com.expertsolution.chesva.model.json.AuthResponse;
import ua.com.expertsolution.chesva.service.sync.SyncServiceApi;
import ua.com.expertsolution.chesva.utils.NetworkUtils;
import ua.com.expertsolution.chesva.utils.SharedStorage;
import ua.com.expertsolution.chesva.utils.StringUtils;

import static ua.com.expertsolution.chesva.common.Consts.APP_CASH_TOKEN_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_SYNC;
import static ua.com.expertsolution.chesva.common.Consts.TOKEN;

@SuppressLint("LongLogTag")
public class LoginViewModel extends AndroidViewModel {

    @Inject
    SyncServiceApi serviceApi;

    private MutableLiveData<LoadEventHandler> resultSync;
    private Context activityContext;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        resultSync = new MutableLiveData<>();
        ((InventoryApplication) application).getComponent().inject(this);
        activityContext = application;
    }

    public void setActivityContext(Context activityContext) {
        this.activityContext = activityContext;
    }

    public LiveData<LoadEventHandler> getResultLoad() {
        return resultSync;
    }

    public Call<AuthResponse> authenticate(String grantType, String userName, String password) {

        resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_STARTED));

        if (!NetworkUtils.checkEthernet(getApplication())) {
            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR, activityContext.getString(R.string.error_internet_connecting)));
            Log.e(TAGLOG_SYNC, activityContext.getString(R.string.error_internet_connecting));
            return null;
        }

        serviceApi.getSyncService().authenticate(grantType, userName, password)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AuthResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) { }

                    @Override
                    public void onNext(AuthResponse response) {
                        if (!TextUtils.isEmpty(response.getError())) {
                            if (!TextUtils.isEmpty(response.getErrorDescription())
                                    && response.getErrorDescription().equals("The user name or password is incorrect.")) {
                                resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR, activityContext.getString(R.string.invalid_login)));
                            }else{
                                resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                            }
                        } else {
                            SharedStorage.setString(getApplication(), APP_CASH_TOKEN_PREFS, TOKEN, response.getTokenType()+" "+response.getAccessToken());
                            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_FINISH));
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (!NetworkUtils.checkEthernet(getApplication())) {
                            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR, activityContext.getString(R.string.error_internet_connecting)));
                            Log.e(TAGLOG_SYNC, activityContext.getString(R.string.error_internet_connecting));
                            return;
                        }

                        if (throwable instanceof HttpException) {
                            ResponseBody body = ((HttpException) throwable).response().errorBody();
                            TypeAdapter<AuthResponse> adapter = new Gson().getAdapter(AuthResponse.class);
                            try {
                                AuthResponse errorParser =
                                        adapter.fromJson(body.string());
                                Log.e(TAGLOG_SYNC, body.string());
                                try {
                                    String errorDescription = "";
                                    if (!TextUtils.isEmpty(errorParser.getError())) {
                                        errorDescription = StringUtils.getStringByIdName(activityContext, errorParser.getError().replace(" ",""));
                                    }
                                    if (TextUtils.isEmpty(errorDescription)) {
                                        errorDescription = errorParser.getErrorDescription();
                                    }
                                    resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR, errorDescription));
                                }catch (Exception exception){
                                    exception.printStackTrace();
                                    resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                                Log.e(TAGLOG_SYNC, e.toString());
                            }
                        } else {
                            Log.e(TAGLOG_SYNC, throwable.toString());
                            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        return null;
    }

}