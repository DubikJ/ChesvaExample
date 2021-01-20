package ua.com.expertsolution.chesva.ui.fragments.download;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.app.InventoryApplication;
import ua.com.expertsolution.chesva.db.DataBase;
import ua.com.expertsolution.chesva.model.LoadEventHandler;
import ua.com.expertsolution.chesva.model.dto.Box;
import ua.com.expertsolution.chesva.model.dto.Condition;
import ua.com.expertsolution.chesva.model.dto.MainAsset;
import ua.com.expertsolution.chesva.model.dto.Person;
import ua.com.expertsolution.chesva.model.json.BoxRequest;
import ua.com.expertsolution.chesva.model.json.BoxResponse;
import ua.com.expertsolution.chesva.model.json.ConditionRequest;
import ua.com.expertsolution.chesva.model.json.ConditionResponse;
import ua.com.expertsolution.chesva.model.json.DownloadResponse;
import ua.com.expertsolution.chesva.model.json.LoadFile;
import ua.com.expertsolution.chesva.model.json.MainAssetRequest;
import ua.com.expertsolution.chesva.model.json.MainAssetResponse;
import ua.com.expertsolution.chesva.model.json.PersonRequest;
import ua.com.expertsolution.chesva.model.json.PersonResponse;
import ua.com.expertsolution.chesva.service.sync.SyncServiceApi;
import ua.com.expertsolution.chesva.utils.NetworkUtils;
import ua.com.expertsolution.chesva.utils.SharedStorage;
import ua.com.expertsolution.chesva.utils.StringUtils;

import static ua.com.expertsolution.chesva.common.Consts.APP_CASH_TOKEN_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.APP_SETTINGS_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.IS_DB_NO_EMPTY;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_DOWNLOAD;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_SYNC;
import static ua.com.expertsolution.chesva.common.Consts.TOKEN;

@SuppressLint("LongLogTag")
public class DownloadViewModel extends AndroidViewModel {
    private static final int LIMIT = 300;
    @Inject
    DataBase dataBase;
    @Inject
    SyncServiceApi serviceApi;

    private MutableLiveData<LoadEventHandler> resultSync;
    private Context activityContext;

    public DownloadViewModel(@NonNull Application application) {
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

    public void loadFromServer(boolean needClear) {

        resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_STARTED));

        String token = SharedStorage.getString(getApplication(),APP_CASH_TOKEN_PREFS, TOKEN, "");

        if(needClear) {

            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_CLEAR_PREVIOUS_DATA));

            Completable.fromAction(() -> dataBase.dataBaseDao().clearDB(getApplication())).observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
                @Override
                public void onSubscribe(Disposable d) { }

                @Override
                public void onComplete() {

                    loadBoxes(token, 0);

                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAGLOG_SYNC, "clearDB onError e:"+e.toString());
                    resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR));
                }
            });
        }else{
            loadBoxes(token, 0);
        }
    }

    public void loadBoxes(String token, int startItem) {{

        if (!NetworkUtils.checkEthernet(getApplication())) {
            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR, activityContext.getString(R.string.error_internet_connecting)));
            Log.e(TAGLOG_SYNC, activityContext.getString(R.string.error_internet_connecting));
            return;
        }

        serviceApi.getSyncService().getBoxList(token, new BoxRequest(LIMIT, startItem))
                .doOnNext(boxResponse -> {
                    dataBase.dataBaseDao().insertBoxList(boxResponse.getBoxes());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BoxResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) { }

                    @Override
                    public void onNext(BoxResponse response) {
                        Log.d(TAGLOG_SYNC, "loadBox onNext onNext "+(startItem+LIMIT)+" from "+response.getTotal());
                        resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_PROGRESS,
                                activityContext.getString(R.string.boxes),
                                initProgress(startItem+LIMIT, response.getTotal()),
                                response.getTotal()));
                        if((startItem+LIMIT)<response.getTotal()){
                            loadBoxes(token, startItem+LIMIT);
                        }else{
                            loadMainAssets(token, 0);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAGLOG_SYNC, "loadBox onError e:"+e.toString());
                        parsingErrorSync(e);
                    }

                    @Override
                    public void onComplete() { }
                });

    }}

    public void loadMainAssets(String token, int startItem) {{

        if (!NetworkUtils.checkEthernet(getApplication())) {
            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR, activityContext.getString(R.string.error_internet_connecting)));
            Log.e(TAGLOG_SYNC, activityContext.getString(R.string.error_internet_connecting));
            return;
        }

        serviceApi.getSyncService().getMainAssetsList(token, new MainAssetRequest(LIMIT, startItem))
                .doOnNext(mainAssetResponse  -> {
                    dataBase.dataBaseDao().insertMainAssetList(mainAssetResponse.getMainAssets());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MainAssetResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(MainAssetResponse response) {
                        Log.d(TAGLOG_SYNC, "loadMainAssets onNext "+(startItem+LIMIT)+" from "+response.getTotal());
                        resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_PROGRESS,
                                activityContext.getString(R.string.main_assets),
                                initProgress(startItem+LIMIT, response.getTotal()),
                                response.getTotal()));
                        if((startItem+LIMIT)<response.getTotal()){
                            loadMainAssets(token, startItem+LIMIT);
                        }else{
                            loadPersons(token, 0);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAGLOG_SYNC, "loadMainAssets onError e:"+e.toString());
                        parsingErrorSync(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }}

    public void loadPersons(String token, int startItem) {{

        if (!NetworkUtils.checkEthernet(getApplication())) {
            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR, activityContext.getString(R.string.error_internet_connecting)));
            Log.e(TAGLOG_SYNC, activityContext.getString(R.string.error_internet_connecting));
            return;
        }

        serviceApi.getSyncService().getPersonList(token, new PersonRequest(LIMIT, startItem))
                .doOnNext(personResponse -> {
                    dataBase.dataBaseDao().insertPersonList(personResponse.getPersons());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<PersonResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(PersonResponse response) {
                        Log.d(TAGLOG_SYNC, "loadConditions onNext "+(startItem+LIMIT)+" from "+response.getTotal());
                        resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_PROGRESS,
                                activityContext.getString(R.string.coditions),
                                initProgress(startItem+LIMIT, response.getTotal()),
                                response.getTotal()));
                        if((startItem+LIMIT)<response.getTotal()){
                            loadPersons(token, startItem+LIMIT);
                        }else{
                            loadConditions(token, 0);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAGLOG_SYNC, "loadPersons onError e:"+e.toString());
                        parsingErrorSync(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }}

    public void loadConditions(String token, int startItem) {{

        if (!NetworkUtils.checkEthernet(getApplication())) {
            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_ERROR, activityContext.getString(R.string.error_internet_connecting)));
            Log.e(TAGLOG_SYNC, activityContext.getString(R.string.error_internet_connecting));
            return;
        }

        serviceApi.getSyncService().getConditionList(token, new ConditionRequest(LIMIT, startItem))
                .doOnNext(conditionResponse -> {
                    dataBase.dataBaseDao().insertConditionList(conditionResponse.getConditions());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ConditionResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(ConditionResponse response) {
                        Log.d(TAGLOG_SYNC, "loadConditions onNext "+(startItem+LIMIT)+" from "+response.getTotal());
                        resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_PROGRESS,
                                activityContext.getString(R.string.coditions),
                                initProgress(startItem+LIMIT, response.getTotal()),
                                response.getTotal()));
                        if((startItem+LIMIT)<response.getTotal()){
                            loadConditions(token, startItem+LIMIT);
                        }else{
                            SharedStorage.setBoolean(getApplication(), APP_SETTINGS_PREFS, IS_DB_NO_EMPTY, true);
                            resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_FINISH));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAGLOG_SYNC, "loadConditions onError e:"+e.toString());
                        parsingErrorSync(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }}

    private int initProgress(int progress, int total){
        if(total == 0){
            return 0;
        }
        return progress > total ? total : total;
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

    public AsyncTask loadFromFile(Uri uriFile, boolean needClear) {

        resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_STARTED));

        return new AsyncTask<Boolean, LoadEventHandler, LoadEventHandler>() {
            @Override
            protected LoadEventHandler doInBackground(Boolean... booleans) {
                if(needClear) {
                    publishProgress(new LoadEventHandler(LoadEventHandler.LOAD_CLEAR_PREVIOUS_DATA));
                    dataBase.dataBaseDao().clearDB(getApplication());
                }
                publishProgress(new LoadEventHandler(LoadEventHandler.LOAD_OPEN_FILE));
//                int elemCount, curPos;
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(getApplication().getContentResolver()
                                    .openInputStream(uriFile)));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    LoadFile loadFile = new Gson().fromJson(sb.toString(), LoadFile.class);

                    if(loadFile!=null) {

                        if(loadFile.getBoxes()!=null && loadFile.getBoxes().size()>0){
                            int i = 1;
                            for(Box box: loadFile.getBoxes()) {
                                publishProgress(new LoadEventHandler(LoadEventHandler.LOAD_PROGRESS,
                                        activityContext.getString(R.string.boxes),
                                        i, loadFile.getBoxes().size()));
                                dataBase.dataBaseDao().insert(box);
                                i++;
                            }
                        }

                        if(loadFile.getMainAssets()!=null && loadFile.getMainAssets().size()>0){
                            int i = 1;
                            for(MainAsset mainAsset: loadFile.getMainAssets()) {
                                publishProgress(new LoadEventHandler(LoadEventHandler.LOAD_PROGRESS,
                                        activityContext.getString(R.string.boxes),
                                        i, loadFile.getBoxes().size()));
                                dataBase.dataBaseDao().insert(mainAsset);
                                i++;
                            }
                        }

                        if(loadFile.getConditions()!=null && loadFile.getConditions().size()>0) {
                            int i = 1;
                            for(Condition condition: loadFile.getConditions()) {
                                publishProgress(new LoadEventHandler(LoadEventHandler.LOAD_PROGRESS,
                                        activityContext.getString(R.string.boxes),
                                        i, loadFile.getBoxes().size()));
                                dataBase.dataBaseDao().insert(condition);
                                i++;
                            }
                        }


                        if(loadFile.getPersons()!=null && loadFile.getPersons().size()>0){
                            int i = 1;
                            for(Person person: loadFile.getPersons()) {
                                publishProgress(new LoadEventHandler(LoadEventHandler.LOAD_PROGRESS,
                                        activityContext.getString(R.string.boxes),
                                        i, loadFile.getBoxes().size()));
                                dataBase.dataBaseDao().insert(person);
                                i++;
                            }
                        }

                        SharedStorage.setBoolean(getApplication(), APP_SETTINGS_PREFS, IS_DB_NO_EMPTY, true);

                    }else{
                        new LoadEventHandler(LoadEventHandler.LOAD_ERROR);
                    }

//                    InputStream inputStream = getApplication().getContentResolver().openInputStream(uriFile);
//                    Object sheet;
//                    Object workbook;
//                    boolean isXLSX = checkXLSX(uriFile);
//                    if (isXLSX) {
//                        workbook = new XSSFWorkbook(inputStream);
//                        sheet = ((XSSFWorkbook) workbook).getSheetAt(0); //XSSFSheet
//                    } else {
//                        workbook = new HSSFWorkbook(inputStream);
//                        sheet = ((HSSFWorkbook) workbook).getSheetAt(0); //HSSFSheet
//                    }
//                    int rowsCount = 0;
//                    FormulaEvaluator formulaEvaluator = null;
//                    if (isXLSX) {
//                        rowsCount = ((XSSFSheet) sheet).getPhysicalNumberOfRows();
//                        formulaEvaluator = ((XSSFWorkbook) workbook).getCreationHelper().createFormulaEvaluator();
//                    } else {
//                        rowsCount = ((HSSFSheet) sheet).getPhysicalNumberOfRows();
//                        formulaEvaluator = ((HSSFWorkbook) workbook).getCreationHelper().createFormulaEvaluator();
//                    }
//                    elemCount = rowsCount - 1;
//                    List<Facility> facilityList = new ArrayList<>();
//                    for (int r = 2; r < rowsCount; r++) {
//                        Row row = null;
//                        if (isXLSX)
//                            row = ((XSSFSheet) sheet).getRow(r);
//                        else
//                            row = ((HSSFSheet) sheet).getRow(r);
//                        int cellsCount = row.getPhysicalNumberOfCells();
//
//                        Facility facility = new Facility();
//                        facility.setState(STATE_IN_SEARCH_OF);
//                        for (int c = 0; c < cellsCount; c++) {
//                            String value = getCellAsString(row, c, formulaEvaluator);
//                            switch (c) {
//                                case 5:
//                                    facility.setInventoryNumber(value);
//                                    break;
//                                case 6:
//                                    facility.setName(value);
//                                    break;
//                                case 12:
//                                    facility.setRfid(value);
//                                    if(!TextUtils.isEmpty(value)){
//                                        if(TextUtils.isEmpty(facility.getField1())){
//                                            facility.setState(STATE_CREATED);
//                                        }else{
//                                            facility.setState(STATE_FOUND);
//                                        }
//                                    }else{
//                                        facility.setState(STATE_IN_SEARCH_OF);
//                                    }
//                                    break;
//                                case 0:
//                                    facility.setField1(value);
//                                    break;
//                                case 1:
//                                    facility.setField2(value);
//                                    break;
//                                case 2:
//                                    facility.setField3(value);
//                                    break;
//                                case 3:
//                                    facility.setField4(value);
//                                    break;
//                                case 4:
//                                    facility.setField5(value);
//                                    break;
//                                case 7:
//                                    facility.setField6(value);
//                                    break;
//                                case 8:
//                                    facility.setField7(value);
//                                    break;
//                                case 9:
//                                    facility.setField8(value);
//                                    break;
//                                case 10:
//                                    facility.setField9(value);
//                                    break;
//                                case 11:
//                                    facility.setField10(value);
//                                    break;
////                                case 12:
////                                    facility.setField10(value);
////                                    break;
////                                case 13:
////                                    facility.setField11(value);
////                                    break;
//                            }
//
//                        }
//                        if (!TextUtils.isEmpty(facility.getName())) {
//                            facilityList.add(facility);
//                        }
//                        curPos = r + 1;
//                        publishProgress(new LoadEventHandler(LoadEventHandler.LOAD_PROGRESS, "", curPos, elemCount));
//                    }

//                    publishProgress(new LoadEventHandler(LoadEventHandler.LOAD_SAVING_TO_DB));
//
//                    Collections.sort(facilityList, (o1, o2) ->{
//                        return o1.getName().compareToIgnoreCase(o2.getName());
//                    });

//                    dataBase.dataBaseDao().insertList(getApplication(), facilityList);

//                    inputStream.close();

                } catch (FileNotFoundException e) {
                    Log.e(TAGLOG_DOWNLOAD, e.toString());
                    return new LoadEventHandler(LoadEventHandler.LOAD_ERROR, activityContext.getString(R.string.error_file));
                } catch (IOException e) {
                    Log.e(TAGLOG_DOWNLOAD, e.toString());
                    return new LoadEventHandler(LoadEventHandler.LOAD_ERROR, activityContext.getString(R.string.error_read));
                } catch (Exception e) {
                    Log.e(TAGLOG_DOWNLOAD, e.toString());
                    return new LoadEventHandler(LoadEventHandler.LOAD_ERROR);
                }
                return new LoadEventHandler(LoadEventHandler.LOAD_FINISH);
            }

            @Override
            protected void onProgressUpdate(LoadEventHandler... values) {
                super.onProgressUpdate(values);
                resultSync.setValue(values[0]);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                resultSync.setValue(new LoadEventHandler(LoadEventHandler.LOAD_DISMISS));
                Log.d(TAGLOG_DOWNLOAD, "Download Cancelled");
            }

            @Override
            protected void onPostExecute(LoadEventHandler loadEventHandler) {
                super.onPostExecute(loadEventHandler);
                resultSync.setValue(loadEventHandler);
            }

        }.execute();

    }

//    private boolean checkXLSX(Uri uri) {
//        boolean res = false;
//        File file= new File(uri.getPath());
//        String filename = file.getName();
//        int pos_ext = filename.lastIndexOf('.');
//        if (pos_ext > 0) {
//            res = ("xlsx".equalsIgnoreCase(filename.substring(pos_ext + 1)));
//        }
//        return res;
//    }
//
//    protected String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
//        String value = "";
//        try {
//            Cell cell = row.getCell(c);
//            CellValue cellValue = formulaEvaluator.evaluate(cell);
//            switch (cellValue.getCellType()) {
//                case Cell.CELL_TYPE_BOOLEAN:
//                    value = ""+cellValue.getBooleanValue();
//                    break;
//                case Cell.CELL_TYPE_NUMERIC:
//                    double numericValue = cellValue.getNumberValue();
//                    if(HSSFDateUtil.isCellDateFormatted(cell)) {
//                        double date = cellValue.getNumberValue();
//                        SimpleDateFormat formatter =
//                                new SimpleDateFormat("dd/MM/yy");
//                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
//                    } else {
//                        value = ""+numericValue;
//                    }
//                    break;
//                case Cell.CELL_TYPE_STRING:
//                    value = ""+cellValue.getStringValue();
//                    break;
//                default:
//            }
//        } catch (NullPointerException e) {
//            Log.i(TAGLOG_DOWNLOAD, e.toString());
//        }
//        return value;
//    }

}