package ua.com.expertsolution.chesva.ui.fragments.upload;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.app.InventoryApplication;
import ua.com.expertsolution.chesva.db.DBConstant;
import ua.com.expertsolution.chesva.db.DataBase;
import ua.com.expertsolution.chesva.model.LoadEventHandler;
import ua.com.expertsolution.chesva.model.dto.Operation;
import ua.com.expertsolution.chesva.model.json.ChangeBoxRfidListRequest;
import ua.com.expertsolution.chesva.model.json.ChangeMainAssetRfidListRequest;
import ua.com.expertsolution.chesva.model.json.DownloadResponse;
import ua.com.expertsolution.chesva.model.json.UpLoadFile;
import ua.com.expertsolution.chesva.scanner.Device;
import ua.com.expertsolution.chesva.service.sync.SyncServiceApi;
import ua.com.expertsolution.chesva.utils.DeviceUtils;
import ua.com.expertsolution.chesva.utils.NetworkUtils;
import ua.com.expertsolution.chesva.utils.SharedStorage;
import ua.com.expertsolution.chesva.utils.StringUtils;

import static ua.com.expertsolution.chesva.common.Consts.APP_CASH_TOKEN_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_DB;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_DOWNLOAD;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_SYNC;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_UPLOAD;
import static ua.com.expertsolution.chesva.common.Consts.TOKEN;

@SuppressLint("LongLogTag")
public class UploadViewModel extends AndroidViewModel {
    private static final int LIMIT = 300;

    @Inject
    DataBase dataBase;
    @Inject
    SyncServiceApi serviceApi;

    private int sizeSendBoxAddRfid;
    private int sizeSendMainAssetAddRfid;
    private int sizeSendMainAssetInBox;
    private int sizeSendMainAssetIssuing;
    private int sizeSendMainAssetReturning;
    private MutableLiveData<LoadEventHandler> resultSync;
    private Context activityContext;

    public UploadViewModel(@NonNull Application application) {
        super(application);
        resultSync = new MutableLiveData<>();
        ((InventoryApplication) application).getComponent().inject(this);
        activityContext = application;
    }

    public void setActivityContext(Context activityContext) {
        this.activityContext = activityContext;
    }

    public LiveData<LoadEventHandler> getResultUpLoad() {
        return resultSync;
    }

    public void uploadToServer() {

        String token = SharedStorage.getString(getApplication(),APP_CASH_TOKEN_PREFS, TOKEN, "");
        resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_STARTED));
        sizeSendBoxAddRfid = 0;
        sizeSendMainAssetAddRfid = 0;

        Completable.fromAction(() -> {
            sizeSendBoxAddRfid = dataBase.dataBaseDao()
                    .getCountListByFilter(DBConstant.OPERATION_TABLE,
                            DBConstant.OPERATION_TYPE_OPERATION + "=" +Operation.TYPE_OPERATION_BOX_ADD_RFID+ " AND "+DBConstant.OPERATION_SEND + "=1");
            sizeSendMainAssetAddRfid = dataBase.dataBaseDao()
                    .getCountListByFilter(DBConstant.OPERATION_TABLE,
                            DBConstant.OPERATION_TYPE_OPERATION + "=" +Operation.TYPE_OPERATION_MAIN_ASSET_ADD_RFID+ " AND "+DBConstant.OPERATION_SEND + "=1");
            sizeSendMainAssetInBox = dataBase.dataBaseDao()
                    .getCountListByFilter(DBConstant.OPERATION_TABLE,
                            DBConstant.OPERATION_TYPE_OPERATION + "=" +Operation.TYPE_OPERATION_MAIN_ASSET_IN_BOX+ " AND "+DBConstant.OPERATION_SEND + "=1");
            sizeSendMainAssetIssuing = dataBase.dataBaseDao()
                    .getCountListByFilter(DBConstant.OPERATION_TABLE,
                            DBConstant.OPERATION_TYPE_OPERATION + "=" +Operation.TYPE_OPERATION_ISSUING_MAIN_ASSET+ " AND "+DBConstant.OPERATION_SEND + "=1");
            sizeSendMainAssetReturning = dataBase.dataBaseDao()
                    .getCountListByFilter(DBConstant.OPERATION_TABLE,
                            DBConstant.OPERATION_TYPE_OPERATION + "=" +Operation.TYPE_OPERATION_RETURNING_MAIN_ASSET+ " AND "+DBConstant.OPERATION_SEND + "=1");
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {}

            @Override
            public void onComplete() {
                uploadBoxesAddRfid(token, 0);
            }

            @Override
            public void onError(Throwable e) {
                resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                Log.i(TAGLOG_DB, e.toString());
            }
        });


    }

    public void uploadBoxesAddRfid(String token, int startItem) {

        if(sizeSendBoxAddRfid ==0){
          upLoadMainAssetsAddRfid(token, 0);
          return;
        }

        if (!NetworkUtils.checkEthernet(getApplication())) {
            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR, activityContext.getString(R.string.error_internet_connecting)));
            Log.e(TAGLOG_SYNC, activityContext.getString(R.string.error_internet_connecting));
            return;
        }

        dataBase.dataBaseDao().getUploadOperationList(Operation.TYPE_OPERATION_BOX_ADD_RFID, startItem, LIMIT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Operation>>() {
                    @Override
                    public void onSubscribe(Disposable d) { }

                    @Override
                    public void onSuccess(List<Operation> operationList) {
                        if(operationList!=null && operationList.size()>0){
                            serviceApi.getSyncService().changeBoxRfidList(token, new ChangeBoxRfidListRequest(operationList))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<DownloadResponse>() {
                                        @Override
                                        public void onSubscribe(Disposable d) { }
                                        @Override
                                        public void onNext(DownloadResponse response) {
                                            Log.d(TAGLOG_SYNC, "uploadBox onNext " + (startItem + LIMIT) + " from " + sizeSendBoxAddRfid);
                                            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_PROGRESS,
                                                    activityContext.getString(R.string.boxes_add_rfid),
                                                    startItem + LIMIT > sizeSendBoxAddRfid ? sizeSendBoxAddRfid : startItem + LIMIT,
                                                    sizeSendBoxAddRfid));

                                            Completable.fromAction(() -> {
                                                dataBase.dataBaseDao().setOperationListSended(operationList);
                                            }).observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                                                @Override
                                                public void onSubscribe(Disposable d) {}

                                                @Override
                                                public void onComplete() {
                                                    if ((startItem + LIMIT) < sizeSendBoxAddRfid) {
                                                        uploadBoxesAddRfid(token, startItem + LIMIT);
                                                    } else {
                                                        upLoadMainAssetsAddRfid(token, 0);
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                                                    Log.i(TAGLOG_DB, e.toString());
                                                }
                                            });

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.e(TAGLOG_SYNC, "loadBox onError e:"+e.toString());
                                            parsingErrorSync(e);
                                        }

                                        @Override
                                        public void onComplete() {
                                        }
                                    });
                        }else{
                            upLoadMainAssetsAddRfid(token, 0);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAGLOG_SYNC, "getListBox onError e:" + t.toString());
                        resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                    }
                });

    }

    public void upLoadMainAssetsAddRfid(String token, int startItem) {

        if(sizeSendMainAssetAddRfid ==0){
            upLoadMainAssetsInBox(token, 0);
            return;
        }

        if (!NetworkUtils.checkEthernet(getApplication())) {
            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR, activityContext.getString(R.string.error_internet_connecting)));
            Log.e(TAGLOG_SYNC, activityContext.getString(R.string.error_internet_connecting));
            return;
        }

        dataBase.dataBaseDao().getUploadOperationList(Operation.TYPE_OPERATION_MAIN_ASSET_ADD_RFID, startItem, LIMIT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Operation>>() {
                    @Override
                    public void onSubscribe(Disposable d) { }

                    @Override
                    public void onSuccess(List<Operation> operationList) {
                        if(operationList!=null && operationList.size()>0){
                            serviceApi.getSyncService().changeMainAssetRfidList(token, new ChangeMainAssetRfidListRequest(operationList))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<DownloadResponse>() {
                                        @Override
                                        public void onSubscribe(Disposable d) { }
                                        @Override
                                        public void onNext(DownloadResponse response) {
                                            Log.d(TAGLOG_SYNC, "uploadMainAsset onNext " + (startItem + LIMIT) + " from " + sizeSendMainAssetAddRfid);
                                            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_PROGRESS,
                                                    activityContext.getString(R.string.main_assets_add_rfid),
                                                    startItem + LIMIT > sizeSendMainAssetAddRfid ? sizeSendMainAssetAddRfid : startItem + LIMIT,
                                                    sizeSendMainAssetAddRfid));

                                            Completable.fromAction(() -> {
                                                dataBase.dataBaseDao().setOperationListSended(operationList);
                                            }).observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                                                @Override
                                                public void onSubscribe(Disposable d) {}

                                                @Override
                                                public void onComplete() {
                                                    if ((startItem + LIMIT) < sizeSendMainAssetAddRfid) {
                                                        upLoadMainAssetsAddRfid(token, startItem + LIMIT);
                                                    } else {
                                                        upLoadMainAssetsInBox(token, 0);
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                                                    Log.i(TAGLOG_DB, e.toString());
                                                }
                                            });

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.e(TAGLOG_SYNC, "loadBox onError e:"+e.toString());
                                            parsingErrorSync(e);
                                        }

                                        @Override
                                        public void onComplete() {
                                        }
                                    });
                        }else{
                            upLoadMainAssetsInBox(token, 0);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAGLOG_SYNC, "getListBox onError e:" + t.toString());
                        resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                    }
                });

    }

    public void upLoadMainAssetsInBox(String token, int startItem) {

        if(sizeSendMainAssetInBox ==0){
            upLoadIssuingMainAssets(token, 0);
            return;
        }

        if (!NetworkUtils.checkEthernet(getApplication())) {
            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR, activityContext.getString(R.string.error_internet_connecting)));
            Log.e(TAGLOG_SYNC, activityContext.getString(R.string.error_internet_connecting));
            return;
        }

        dataBase.dataBaseDao().getUploadOperationList(Operation.TYPE_OPERATION_MAIN_ASSET_IN_BOX, startItem, LIMIT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Operation>>() {
                    @Override
                    public void onSubscribe(Disposable d) { }

                    @Override
                    public void onSuccess(List<Operation> operationList) {
                        if(operationList!=null && operationList.size()>0){
                            serviceApi.getSyncService().changeMainAssetChangeBoxList(token, new ChangeMainAssetRfidListRequest(operationList))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<DownloadResponse>() {
                                        @Override
                                        public void onSubscribe(Disposable d) { }
                                        @Override
                                        public void onNext(DownloadResponse response) {
                                            Log.d(TAGLOG_SYNC, "uploadMainAsset onNext " + (startItem + LIMIT) + " from " + sizeSendMainAssetAddRfid);
                                            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_PROGRESS,
                                                    activityContext.getString(R.string.main_assets_add_rfid),
                                                    startItem + LIMIT > sizeSendMainAssetAddRfid ? sizeSendMainAssetAddRfid : startItem + LIMIT,
                                                    sizeSendMainAssetAddRfid));

                                            Completable.fromAction(() -> {
                                                dataBase.dataBaseDao().setOperationListSended(operationList);
                                            }).observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                                                @Override
                                                public void onSubscribe(Disposable d) {}

                                                @Override
                                                public void onComplete() {
                                                    if ((startItem + LIMIT) < sizeSendMainAssetAddRfid) {
                                                        upLoadMainAssetsInBox(token, startItem + LIMIT);
                                                    } else {
                                                        upLoadIssuingMainAssets(token, 0);
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                                                    Log.i(TAGLOG_DB, e.toString());
                                                }
                                            });

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.e(TAGLOG_SYNC, "loadBox onError e:"+e.toString());
                                            parsingErrorSync(e);
                                        }

                                        @Override
                                        public void onComplete() {
                                        }
                                    });
                        }else{
                            upLoadIssuingMainAssets(token, 0);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAGLOG_SYNC, "getListBox onError e:" + t.toString());
                        resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                    }
                });

    }

    public void upLoadIssuingMainAssets(String token, int startItem) {

        if(sizeSendMainAssetIssuing ==0){
            upLoadReturningMainAssets(token, 0);
            return;
        }

        if (!NetworkUtils.checkEthernet(getApplication())) {
            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR, activityContext.getString(R.string.error_internet_connecting)));
            Log.e(TAGLOG_SYNC, activityContext.getString(R.string.error_internet_connecting));
            return;
        }

        dataBase.dataBaseDao().getUploadOperationList(Operation.TYPE_OPERATION_ISSUING_MAIN_ASSET, startItem, LIMIT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Operation>>() {
                    @Override
                    public void onSubscribe(Disposable d) { }

                    @Override
                    public void onSuccess(List<Operation> operationList) {
                        if(operationList!=null && operationList.size()>0){
                            serviceApi.getSyncService().changeMainAssetChangePersonList(token, new ChangeMainAssetRfidListRequest(operationList))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<DownloadResponse>() {
                                        @Override
                                        public void onSubscribe(Disposable d) { }
                                        @Override
                                        public void onNext(DownloadResponse response) {
                                            Log.d(TAGLOG_SYNC, "uploadMainAsset onNext " + (startItem + LIMIT) + " from " + sizeSendMainAssetAddRfid);
                                            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_PROGRESS,
                                                    activityContext.getString(R.string.main_assets_add_rfid),
                                                    startItem + LIMIT > sizeSendMainAssetAddRfid ? sizeSendMainAssetAddRfid : startItem + LIMIT,
                                                    sizeSendMainAssetAddRfid));

                                            Completable.fromAction(() -> {
                                                dataBase.dataBaseDao().setOperationListSended(operationList);
                                            }).observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                                                @Override
                                                public void onSubscribe(Disposable d) {}

                                                @Override
                                                public void onComplete() {
                                                    if ((startItem + LIMIT) < sizeSendMainAssetAddRfid) {
                                                        upLoadIssuingMainAssets(token, startItem + LIMIT);
                                                    } else {
                                                        upLoadReturningMainAssets(token, 0);
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                                                    Log.i(TAGLOG_DB, e.toString());
                                                }
                                            });

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.e(TAGLOG_SYNC, "loadBox onError e:"+e.toString());
                                            parsingErrorSync(e);
                                        }

                                        @Override
                                        public void onComplete() {
                                        }
                                    });
                        }else{
                            upLoadReturningMainAssets(token, 0);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAGLOG_SYNC, "getListBox onError e:" + t.toString());
                        resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                    }
                });

    }

    public void upLoadReturningMainAssets(String token, int startItem) {

        if(sizeSendMainAssetReturning ==0){
            resultSync.setValue(new LoadEventHandler(LoadEventHandler.UPLOAD_FINISH));
            return;
        }

        if (!NetworkUtils.checkEthernet(getApplication())) {
            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR, activityContext.getString(R.string.error_internet_connecting)));
            Log.e(TAGLOG_SYNC, activityContext.getString(R.string.error_internet_connecting));
            return;
        }

        dataBase.dataBaseDao().getUploadOperationList(Operation.TYPE_OPERATION_RETURNING_MAIN_ASSET, startItem, LIMIT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Operation>>() {
                    @Override
                    public void onSubscribe(Disposable d) { }

                    @Override
                    public void onSuccess(List<Operation> operationList) {
                        if(operationList!=null && operationList.size()>0){
                            serviceApi.getSyncService().changeMainAssetChangePersonList(token, new ChangeMainAssetRfidListRequest(operationList))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<DownloadResponse>() {
                                        @Override
                                        public void onSubscribe(Disposable d) { }
                                        @Override
                                        public void onNext(DownloadResponse response) {
                                            Log.d(TAGLOG_SYNC, "uploadMainAsset onNext " + (startItem + LIMIT) + " from " + sizeSendMainAssetAddRfid);
                                            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_PROGRESS,
                                                    activityContext.getString(R.string.main_assets_add_rfid),
                                                    startItem + LIMIT > sizeSendMainAssetAddRfid ? sizeSendMainAssetAddRfid : startItem + LIMIT,
                                                    sizeSendMainAssetAddRfid));

                                            Completable.fromAction(() -> {
                                                dataBase.dataBaseDao().setOperationListSended(operationList);
                                            }).observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                                                @Override
                                                public void onSubscribe(Disposable d) {}

                                                @Override
                                                public void onComplete() {
                                                    if ((startItem + LIMIT) < sizeSendMainAssetAddRfid) {
                                                        upLoadReturningMainAssets(token, startItem + LIMIT);
                                                    } else {
                                                        resultSync.setValue(new LoadEventHandler(LoadEventHandler.UPLOAD_FINISH));
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                                                    Log.i(TAGLOG_DB, e.toString());
                                                }
                                            });

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.e(TAGLOG_SYNC, "loadBox onError e:"+e.toString());
                                            parsingErrorSync(e);
                                        }

                                        @Override
                                        public void onComplete() {
                                        }
                                    });
                        }else{
                            resultSync.setValue(new LoadEventHandler(LoadEventHandler.UPLOAD_FINISH));
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAGLOG_SYNC, "getListBox onError e:" + t.toString());
                        resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                    }
                });

    }

    private void parsingErrorSync(Throwable throwable){
        if (throwable instanceof HttpException) {
            ResponseBody body = ((HttpException) throwable).response().errorBody();
            TypeAdapter<DownloadResponse> adapter = new Gson().getAdapter(DownloadResponse.class);
            try {
                DownloadResponse errorParser = adapter.fromJson(body.string());
                Log.e(TAGLOG_SYNC, body.string());

                try {
                    String errorDescription = "";
                    if (!TextUtils.isEmpty(errorParser.getCode())) {
                        errorDescription = StringUtils.getStringByIdName(activityContext, errorParser.getCode());
                    }
                    if (TextUtils.isEmpty(errorDescription)) {
                        errorDescription = errorParser.getMessage();
                    }
                    resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR, errorDescription));
                }catch (Exception exception){
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

    public AsyncTask writeToFile(Uri uriFile) {

        resultSync.setValue(new LoadEventHandler(LoadEventHandler.UPLOAD_STARTED));

        return new AsyncTask<Boolean, LoadEventHandler, LoadEventHandler>() {
            @Override
            protected LoadEventHandler doInBackground(Boolean... booleans) {

                try {

                    UpLoadFile upLoadFile = UpLoadFile.builder()
                            .code("" + UUID.randomUUID())
                            .userID(0)
                            .boxesRfid(dataBase.dataBaseDao().getOperationListByFilter(
                                    DBConstant.OPERATION_TYPE_OPERATION + "=" + Operation.TYPE_OPERATION_BOX_ADD_RFID
                                            + " AND " + DBConstant.OPERATION_SEND + "=1"))
                            .changePersons(dataBase.dataBaseDao().getOperationListByFilter(
                                    "(" + DBConstant.OPERATION_TYPE_OPERATION + "=" + Operation.TYPE_OPERATION_ISSUING_MAIN_ASSET + " OR " +
                                            DBConstant.OPERATION_TYPE_OPERATION + "=" + Operation.TYPE_OPERATION_RETURNING_MAIN_ASSET + ")"
                                            + " AND " + DBConstant.OPERATION_SEND + "=1"))
                            .changeBoxes(dataBase.dataBaseDao().getOperationListByFilter(
                                    DBConstant.OPERATION_TYPE_OPERATION + "=" + Operation.TYPE_OPERATION_MAIN_ASSET_IN_BOX
                                            + " AND " + DBConstant.OPERATION_SEND + "=1"))
                            .changeMainAssetRfids(dataBase.dataBaseDao().getOperationListByFilter(
                                    DBConstant.OPERATION_TYPE_OPERATION + "=" + Operation.TYPE_OPERATION_MAIN_ASSET_ADD_RFID
                                            + " AND " + DBConstant.OPERATION_SEND + "=1"))
                            .build();

                    publishProgress(new LoadEventHandler(LoadEventHandler.UPLOAD_SAVING_TO_FILE));

                    String pathFile = uriFile.getPath();
                    try {
                        pathFile = uriFile.getPath().split(":")[1];
                        pathFile = android.os.Environment.getExternalStorageDirectory() + "/" + pathFile.replaceAll(".fid", "") + ".fid";
                        File oldFile = new File(uriFile.getPath());
                        oldFile.delete();
                    } catch (Exception e) {
                    }

                    File resultFile = new File(pathFile);
                    if(!resultFile.exists()) {
                        resultFile.createNewFile();
                    }

                    OutputStream outputStream = new FileOutputStream(pathFile);
                    outputStream.write(new Gson().toJson(upLoadFile).getBytes());
                    outputStream.flush();
                    outputStream.close();


                    MediaScannerConnection.scanFile(getApplication(), new String[]{resultFile.getPath()}, new String[]{""}, null);

                    dataBase.dataBaseDao().setOperationListSended(dataBase.dataBaseDao().getOperationListByFilter(DBConstant.OPERATION_SEND + "=1"));

                } catch (IOException e) {
                    Log.e(TAGLOG_UPLOAD, e.toString());
                    e.printStackTrace();
                    return new LoadEventHandler(LoadEventHandler.UPLOAD_ERROR, activityContext.getString(R.string.error_write_file));
                } catch (Exception e) {
                    Log.e(TAGLOG_UPLOAD, e.toString());
                    return new LoadEventHandler(LoadEventHandler.UPLOAD_ERROR);
                }
                return new LoadEventHandler(LoadEventHandler.UPLOAD_FINISH);
            }

            @Override
            protected void onProgressUpdate(LoadEventHandler... values) {
                super.onProgressUpdate(values);
                resultSync.setValue(values[0]);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                resultSync.setValue(new LoadEventHandler(LoadEventHandler.UPLOAD_DISMISS));
                Log.d(TAGLOG_DOWNLOAD, "Upload Cancelled");
            }

            @Override
            protected void onPostExecute(LoadEventHandler loadEventHandler) {
                super.onPostExecute(loadEventHandler);
                resultSync.setValue(loadEventHandler);
            }

        }.execute();

//        return new AsyncTask<Boolean, LoadEventHandler, LoadEventHandler>() {
//            @Override
//            protected LoadEventHandler doInBackground(Boolean... booleans) {
//                List<Facility> facilityList = new ArrayList<>();//dataBase.dataBaseDao().getFacilityListToUpload();
//                InputStream stream = getApplication().getResources().openRawResource(R.raw.template);
//                try {
//                    XSSFWorkbook workbook = new XSSFWorkbook(stream);
//                    XSSFSheet sheet = workbook.getSheetAt(0);
//
//                    XSSFCellStyle style = workbook.createCellStyle();
//                    style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
//                    style.setBorderTop(HSSFCellStyle.BORDER_THIN);
//                    style.setBorderRight(HSSFCellStyle.BORDER_THIN);
//                    style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//
//                    XSSFCellStyle styleBottom = workbook.createCellStyle();
//                    styleBottom.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
//                    styleBottom.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
//                    styleBottom.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
//                    styleBottom.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
//
//                    int i = 0;
//                    Cell cell = null;
//                    for (Facility facility : facilityList) {
//                        Row row = sheet.createRow(i + 1 + 1);
//
//
//                        cell = row.createCell(0);
//                        cell.setCellValue(facility.getField1());
//                        cell.setCellStyle(style);
//
//
//                        cell = row.createCell(1);
//                        cell.setCellValue(facility.getField2());
//                        cell.setCellStyle(style);
//
//                        cell = row.createCell(2);
//                        cell.setCellValue(facility.getField3());
//                        cell.setCellStyle(style);
//
//                        cell = row.createCell(3);
//                        cell.setCellValue(facility.getField4());
//                        cell.setCellStyle(style);
//
//                        cell = row.createCell(4);
//                        cell.setCellValue(facility.getField5());
//                        cell.setCellStyle(style);
//
//                        cell = row.createCell(5);
//                        cell.setCellValue(facility.getInventoryNumber());
//                        cell.setCellStyle(style);
//
//                        cell = row.createCell(6);
//                        cell.setCellValue(facility.getName());
//                        cell.setCellStyle(style);
//
//                        cell = row.createCell(7);
//                        cell.setCellValue(facility.getField6());
//                        cell.setCellStyle(style);
//
//                        cell = row.createCell(8);
//                        cell.setCellValue(facility.getField7());
//                        cell.setCellStyle(style);
//
//                        cell = row.createCell(9);
//                        cell.setCellValue(facility.getField8());
//                        cell.setCellStyle(style);
//
//                        cell = row.createCell(10);
//                        cell.setCellValue(facility.getField9());
//                        cell.setCellStyle(style);
//
//                        cell = row.createCell(11);
//                        cell.setCellValue(facility.getField10());
//                        cell.setCellStyle(style);
//
//                        cell = row.createCell(12);
//                        cell.setCellValue(facility.getRfid());
//                        cell.setCellStyle(style);
//
////                        cell = row.createCell(13);
////                        cell.setCellValue(facility.getField11());
////                        cell.setCellStyle(style);
////
////                        cell = row.createCell(14);
////                        cell.setCellValue(facility.getState());
////                        cell.setCellStyle(style);
//
//                        publishProgress(new LoadEventHandler(LoadEventHandler.UPLOAD_PROGRESS, "", i, facilityList.size()));
//                        i++;
//                    }
//
//                    publishProgress(new LoadEventHandler(LoadEventHandler.UPLOAD_SAVING_TO_FILE));
//
//                    String pathFile = uriFile.getPath();
//                    try {
//                        pathFile = uriFile.getPath().split(":")[1];
//                        pathFile = android.os.Environment.getExternalStorageDirectory() + "/" + pathFile + ".xlsx";
//                        File oldFile = new File(uriFile.getPath());
//                        //   if(!oldFile.exists()) oldFile.createNewFile();
//                        oldFile.delete();
//                    } catch (Exception e) {
//                    }
//
//                    File resultFile = new File(pathFile);
//                    resultFile.createNewFile();
//
//                    OutputStream outputStream = new FileOutputStream(pathFile);
//                    workbook.write(outputStream);
//                    outputStream.flush();
//                    outputStream.close();
//
//
//                    MediaScannerConnection.scanFile(getApplication(), new String[]{resultFile.getPath()}, new String[]{""}, null);
//
//                } catch (IOException e) {
//                    Log.e(TAGLOG_UPLOAD, e.toString());
//                    e.printStackTrace();
//                    return new LoadEventHandler(LoadEventHandler.UPLOAD_ERROR, getApplication().getString(R.string.error_write_file));
//                } catch (Exception e) {
//                    Log.e(TAGLOG_UPLOAD, e.toString());
//                    return new LoadEventHandler(LoadEventHandler.UPLOAD_ERROR);
//                }
//                return new LoadEventHandler(LoadEventHandler.UPLOAD_FINISH);
//            }
//
//            @Override
//            protected void onProgressUpdate(LoadEventHandler... values) {
//                super.onProgressUpdate(values);
//                resultSync.setValue(values[0]);
//            }
//
//            @Override
//            protected void onCancelled() {
//                super.onCancelled();
//                resultSync.setValue(new LoadEventHandler(LoadEventHandler.UPLOAD_DISMISS));
//                Log.d(TAGLOG_DOWNLOAD, "Upload Cancelled");
//            }
//
//            @Override
//            protected void onPostExecute(LoadEventHandler loadEventHandler) {
//                super.onPostExecute(loadEventHandler);
//                resultSync.setValue(loadEventHandler);
//            }
//
//        }.execute();

    }

//    private class UpLoadData extends AsyncTask<Void, Void, Boolean> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            if(callBackListener!=null){
//                callBackListener.onStartSave();
//            }
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//
//            inventoryList = ((MainActivity)act).getIl();
//            inventoryList.loadListForupload( 0, 0, false);
//            InventoryElement inventoryElement;
//            InputStream stream = act.getResources().openRawResource(R.raw.template);
//            Cell cell = null;
//            CellRangeAddress cellMerge = null;
//            try {
//                XSSFWorkbook workbook = new XSSFWorkbook(stream);
//                XSSFSheet sheet = workbook.getSheetAt(0);
//
//                XSSFCellStyle style = workbook.createCellStyle();
//                style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
//                style.setBorderTop(HSSFCellStyle.BORDER_THIN);
//                style.setBorderRight(HSSFCellStyle.BORDER_THIN);
//                style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//
//                XSSFCellStyle styleBottom = workbook.createCellStyle();
//                styleBottom.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
//                styleBottom.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
//                styleBottom.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
//                styleBottom.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
//                int i = 1;
//                int j = 1;
//                int page = 1;
//                int pageIStart = 1;
//                int pageI = 1;
//                int pageCount = 1;
//                int confirmed = 0;
//                int notFound = 0;
//                int confirmedAll = 0;
//                int notFoundAll = 0;
//                for (InventoryElement ie: inventoryList.getLst()) {
//                    Row row = sheet.createRow(i + 1);
//
//                    insertRow(ie, row, j, style);
//
//                    if (ie.getState() == STATE_CONFIRMED || ie.getState() == STATE_FOUND) {
//                        confirmed++;
//                        confirmedAll++;
//                    }
//
//                    if (ie.getState() == STATE_IN_SEARCH_OF) {
//                        notFound++;
//                        notFoundAll++;
//                    }
//
//                    if (callBackListener != null && act != null) {
//                        final int finalI = j;
//                        act.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                callBackListener.onSaveRow(finalI, inventoryList.getLst().size());
//                            }
//                        });
//                    }
//
//                    if (((page == 1 && pageCount == 20) || pageCount == 40) && j < inventoryList.getLst().size()) {
//                        i++;
//                        Row rowBottom = sheet.createRow(i + 1);
//                        insertBottomRow(rowBottom, page, confirmed, notFound, false, styleBottom);
//                        i++;
//                        Row rowTextBottom = sheet.createRow(i + 1);
//                        insertBottomText(rowTextBottom, pageIStart, pageI, false);
//                        page++;
//                        pageIStart = pageI + 1;
//                        pageCount = 0;
//                        i = i + 5;
//                        confirmed = 0;
//                        notFound = 0;
//
//                        i++;
//                        addcapToTAble(sheet, i);
//
//                    }
//
//                    i++;
//                    j++;
//                    pageI++;
//                    pageCount++;
//
//                }
//                Row rowBottom = sheet.createRow(i + 1);
//                insertBottomRow(rowBottom, page, confirmed, notFound, false, styleBottom);
//                i++;
//                Row rowBottomAll = sheet.createRow(i + 1);
//                insertBottomRow(rowBottomAll, page, confirmedAll, notFoundAll, true, styleBottom);
//                i++;
//                Row rowTextBottom = sheet.createRow(i + 1);
//                insertBottomText(rowTextBottom, pageIStart, pageI-1, false);
//                i++;
//                Row rowTextBottomAll = sheet.createRow(i + 1);
//                insertBottomText(rowTextBottomAll, 1, j-1, true);
//
//                String pathFile = uri.getPath();
//                try{
//                    pathFile = uri.getPath().split(":")[1];
//                    pathFile = android.os.Environment.getExternalStorageDirectory()+"/"+pathFile+".xlsx";
//                    File oldFile = new File(uri.getPath());
//                    //   if(!oldFile.exists()) oldFile.createNewFile();
//                    oldFile.delete();
//                } catch (Exception e){ }
//
//                File resultFile = new File(pathFile);
//                resultFile.createNewFile();
//
//                OutputStream outputStream = new FileOutputStream(pathFile);
//                workbook.write(outputStream);
//                outputStream.flush();
//                outputStream.close();
//
//
//                MediaScannerConnection.scanFile(act, new String[] { resultFile.getPath() }, new String[] { "" }, null);
//
//                if(callBackListener!=null && act!=null){
//                    act.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            callBackListener.onSaveFile();
//                        }
//                    });
//                }
//
//            } catch (Exception e) {
//                Log.d(LOG_ALIAS, e.toString());
//                if(callBackListener!=null && act!=null){
//                    act.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            callBackListener.onError();
//                        }
//                    });
//                    return true;
//                }
//            }
//
//            return false;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean isError) {
//            super.onPostExecute(isError);
//            if(!isError && callBackListener!=null){
//                callBackListener.onFinishSave();
//            }
//        }
//    }
//
//    private void insertRow(InventoryElement ie, Row row, int i, XSSFCellStyle style){
//        Cell cell = null;
//
//        cell = row.createCell(0);
//        cell.setCellValue(i);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(1);
//        cell.setCellValue(ie.getName());
//        cell.setCellStyle(style);
//
//        cell = row.createCell(2);
//        cell.setCellValue(ie.getMainNO());
//        cell.setCellStyle(style);
//
//        cell = row.createCell(3);
//        cell.setCellValue(ie.getLabelNum());
//        cell.setCellStyle(style);
//
//        cell = row.createCell(4);
//        cell.setCellValue(ie.getCapitalizedOn());
//        cell.setCellStyle(style);
//
//        cell = row.createCell(5);
//        cell.setCellValue(ie.getLocation());
//        cell.setCellStyle(style);
//
//        cell = row.createCell(6);
//        cell.setCellValue(ie.getSerialNumber());
//        cell.setCellStyle(style);
//
//        cell = row.createCell(7);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(8);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(9);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(10);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(11);
//        if(ie.getState()==STATE_CONFIRMED || ie.getState()==STATE_FOUND || ie.getState()==STATE_EXCESSIVE){
//            cell.setCellValue(1);
//        }else{
//            cell.setCellValue(0);
//        }
//        cell.setCellStyle(style);
//
//        cell = row.createCell(12);
//        if(ie.getState()==STATE_CONFIRMED || ie.getState()==STATE_FOUND || ie.getState()==STATE_EXCESSIVE){
//            cell.setCellValue(0);
//        }else{
//            cell.setCellValue(-1);
//        }
//        cell.setCellStyle(style);
//
//        cell = row.createCell(13);
//        cell.setCellValue(ie.getLeasingSupplementaryText());
//        cell.setCellStyle(style);
//
//        cell = row.createCell(14);
//        cell.setCellValue(ie.getLocationName());
//        cell.setCellStyle(style);
//
//        cell = row.createCell(15);
//        cell.setCellValue(ie.getDepartmentName());
//
//        cell.setCellStyle(style);
//    }
//
//    private void insertBottomRow(Row row, int page, int confirmed, int notFound, boolean isAll, XSSFCellStyle style){
//        Cell cell = null;
//
//        cell = row.createCell(0);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(1);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(2, 3);
//        if(isAll){
//            cell.setCellValue("Усього по опису:           ");
//        }else {
//            cell.setCellValue("УСЬОГО по сторінці " + page + ":");
//        }
//        cell.setCellStyle(style);
//
//        cell = row.createCell(3);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(4);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(5);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(6);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(7);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(8);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(9);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(10);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(11);
//        cell.setCellValue(confirmed);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(12);
//        cell.setCellValue(notFound);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(13);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(14);
//        cell.setCellStyle(style);
//
//        cell = row.createCell(15);
//        cell.setCellStyle(style);
//    }
//
//    private void insertBottomText(Row row, int from, int to, boolean isAll){
//        Cell cell = row.createCell(1);
//        if(isAll){
//            cell.setCellValue("Усього по опису порядкових номерів з №"+from+" до №" + to);
//        }else {
//            cell.setCellValue("Усього по сторінці порядкових номерів з №"+from+" до №" + to);
//        }
//    }
//
//    private void addcapToTAble(XSSFSheet sheet, int i){
//        Row capRow = sheet.getRow(0);
//        Row capRowPage = sheet.createRow(i);
//        cloneRow(capRow, capRowPage);
//        Row capRow1 = sheet.getRow(1);
//        Row capRowPage1 = sheet.createRow(i+1);
//        cloneRow(capRow1, capRowPage1);
//        mergedRegion(sheet, capRowPage, capRowPage1, 1, 1);
//        mergedRegion(sheet, capRowPage, capRowPage1, 4, 4);
//        mergedRegion(sheet, capRowPage, capRowPage1, 5, 5);
//        mergedRegion(sheet, capRowPage, capRowPage, 2, 3);
//        mergedRegion(sheet, capRowPage, capRowPage, 7, 10);
//        mergedRegion(sheet, capRowPage, capRowPage, 11, 12);
//        mergedRegion(sheet, capRowPage, capRowPage, 13, 15);
//    }
//
//    private void cloneRow(Row rowFrom, Row rowTo){
//        for(int i=0; i<=15; i++) {
//            cloneCell(i, rowFrom, rowTo);
//        }
//    }
//
//    private void cloneCell(int index, Row rowFrom, Row rowTo){
//        Cell cellFrom = rowFrom.getCell(index);
//        Cell cellto = rowTo.createCell(index);
//        cellto.setCellValue(cellFrom.getStringCellValue());
//        cellto.setCellStyle(cellFrom.getCellStyle());
//    }
//
//    private void mergedRegion(XSSFSheet sheet, Row firstRow, Row secondRow, int firstCol, int secondCol){
//        CellRangeAddress cellMerge = new CellRangeAddress(firstRow.getRowNum(), secondRow.getRowNum(), firstCol, secondCol);
//        sheet.addMergedRegion(cellMerge);
//    }

}