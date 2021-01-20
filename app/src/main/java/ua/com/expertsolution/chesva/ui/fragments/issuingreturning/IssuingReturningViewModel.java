package ua.com.expertsolution.chesva.ui.fragments.issuingreturning;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.app.InventoryApplication;
import ua.com.expertsolution.chesva.db.DBConstant;
import ua.com.expertsolution.chesva.db.DataBase;
import ua.com.expertsolution.chesva.model.LoadFiltersHandler;
import ua.com.expertsolution.chesva.model.LoadItemEventHandler;
import ua.com.expertsolution.chesva.model.LoadListEventHandler;
import ua.com.expertsolution.chesva.model.LoadStatusHandler;
import ua.com.expertsolution.chesva.model.dto.MainAsset;
import ua.com.expertsolution.chesva.model.dto.Operation;
import ua.com.expertsolution.chesva.model.dto.Person;

import static ua.com.expertsolution.chesva.adapter.FilterAutoCompleteAdapter.SIZE_FILTER_LIST;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_LOAD;

@SuppressLint("LongLogTag")
public class IssuingReturningViewModel extends AndroidViewModel {

    @Inject
    DataBase dataBase;

    private MutableLiveData<LoadStatusHandler> resultStatus;
    private MutableLiveData<LoadListEventHandler> resultList;
    private MutableLiveData<LoadFiltersHandler> resultFilter;
    private MutableLiveData<LoadListEventHandler> resultFound;
    private MutableLiveData<LoadItemEventHandler> resultSave;
    private List<MainAsset> mainAssetListAll;
    private Context activityContext;

    public IssuingReturningViewModel(@NonNull Application application) {
        super(application);
        resultStatus = new MutableLiveData<>();
        resultList = new MutableLiveData<>();
        resultFilter = new MutableLiveData<>();
        resultFound = new MutableLiveData<>();
        resultSave = new MutableLiveData<>();
        ((InventoryApplication) application).getComponent().inject(this);
        activityContext = application;
    }

    public void setActivityContext(Context activityContext) {
        this.activityContext = activityContext;
    }

    public LiveData<LoadStatusHandler> getResultStatus() {
        return resultStatus;
    }

    public LiveData<LoadListEventHandler> getResultList() {
        return resultList;
    }

    public LiveData<LoadFiltersHandler> getResultFilter() {
        return resultFilter;
    }

    public LiveData<LoadListEventHandler> getResultFound() {
        return resultFound;
    }

    public LiveData<LoadItemEventHandler> getResultSave() {
        return resultSave;
    }

    public AsyncTask updateStatuses(int personID, boolean isReturn) {

        return new AsyncTask<Void, Void, LoadStatusHandler>() {
            @Override
            protected LoadStatusHandler doInBackground(Void... booleans) {
                try {
                    return LoadStatusHandler.builder()
                            .found(dataBase.dataBaseDao().getCountListByFilter(DBConstant.MAIN_ASSET_TABLE,
                                    (isReturn ?
                                            DBConstant.MAIN_ASSET_PERSON_ID_OLD + "=" + personID :
                                            DBConstant.MAIN_ASSET_PERSON_ID + "=" + personID)))
                            .build();
                } catch (Exception e) {
                    Log.e(TAGLOG_LOAD, e.toString());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(LoadStatusHandler loadStatusHandler) {
                super.onPostExecute(loadStatusHandler);
                resultStatus.setValue(loadStatusHandler);
            }

        }.execute();

    }

    public AsyncTask getMainAssetList(int startId, int limit, int personID, boolean isReturn) {

        resultList.setValue(new LoadListEventHandler.Builder().status(LoadListEventHandler.LOAD_STARTED).build());

        return new AsyncTask<Boolean, LoadListEventHandler, LoadListEventHandler>() {
            @Override
            protected LoadListEventHandler doInBackground(Boolean... booleans) {
                List<MainAsset> mainAssets = new ArrayList<>();
                try {
                   if(mainAssetListAll == null || mainAssetListAll.size()==0 || startId == 0) {
                        mainAssetListAll = dataBase.dataBaseDao().getMainAssetListByFilter(
                                isReturn ?
                                        DBConstant.MAIN_ASSET_PERSON_ID_OLD + "=" + personID :
                                        DBConstant.MAIN_ASSET_PERSON_ID + "=" + personID);
                    }
                    if(mainAssetListAll.size()<startId+limit){
                        mainAssets = mainAssetListAll;
                    }else{
                        mainAssets = mainAssetListAll.subList(startId,
                                (startId+limit)>mainAssetListAll.size()? mainAssetListAll.size() :startId+limit);
                    }

                } catch (Exception e) {
                    Log.e(TAGLOG_LOAD, e.toString());
                return new LoadListEventHandler.Builder().status(LoadListEventHandler.LOAD_ERROR).build();
                }
                return new LoadListEventHandler.Builder().status(LoadListEventHandler.LOAD_FINISH).mainAssets(mainAssets).build();
            }

            @Override
            protected void onPostExecute(LoadListEventHandler LoadListEventHandler) {
                super.onPostExecute(LoadListEventHandler);
                resultList.setValue(LoadListEventHandler);
            }

        }.execute();

    }

    public AsyncTask loadFilters(String column, String value) {

        return new AsyncTask<Void, Void, LoadFiltersHandler>() {
            @Override
            protected LoadFiltersHandler doInBackground(Void... booleans) {
                try {
                    return new LoadFiltersHandler(column,
                            dataBase.dataBaseDao().getFilterPersonListByColumn(SIZE_FILTER_LIST, column, value.toUpperCase()));
                } catch (Exception e) {
                    Log.e(TAGLOG_LOAD, e.toString());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(LoadFiltersHandler loadFiltersHandler) {
                super.onPostExecute(loadFiltersHandler);
                resultFilter.setValue(loadFiltersHandler);
            }

        }.execute();

    }


    public AsyncTask saveOperation(Operation operation, Person person) {

        resultSave.setValue(new LoadItemEventHandler(LoadItemEventHandler.LOAD_STARTED));

        return new AsyncTask<Void, Void, LoadItemEventHandler>() {
            @Override
            protected LoadItemEventHandler doInBackground(Void... booleans) {
                try {

                    String query =  "SELECT * FROM "+DBConstant.OPERATION_TABLE +
                            " WHERE (" + DBConstant.OPERATION_ID_OWNER + "=" + operation.getIdOwner() +
                            " AND (" + DBConstant.OPERATION_TYPE_OPERATION + "=" + Operation.TYPE_OPERATION_ISSUING_MAIN_ASSET +
                            " OR " + DBConstant.OPERATION_TYPE_OPERATION + "=" + Operation.TYPE_OPERATION_RETURNING_MAIN_ASSET + ")"
                            + ")"
                            + " ORDER BY " + DBConstant.OPERATION_ID + " DESC LIMIT 1";
                    List<Operation> founded = dataBase.dataBaseDao().getOperationListByQuery(new SimpleSQLiteQuery(query));

                    MainAsset mainAsset = dataBase.dataBaseDao().getMainAssetById(operation.getIdOwner());

                    String error = "";
                    if (founded != null && founded.size()>0) {
                        Operation foundedOperation = founded.get(0);
                        if(operation.getTypeOperation() == Operation.TYPE_OPERATION_ISSUING_MAIN_ASSET) {
                            if (foundedOperation.getTypeOperation() == Operation.TYPE_OPERATION_ISSUING_MAIN_ASSET) {
                                if (foundedOperation.getPersonID() == operation.getPersonID()) {
                                    error = activityContext.getString(R.string.main_asset) + " " + operation.getOwnerName() + " " +
                                            activityContext.getString(R.string.this_main_asset_issued) + " " +
                                            activityContext.getString(R.string.this_person);
                                } else {
                                    error = activityContext.getString(R.string.main_asset) + " " + operation.getOwnerName() + " " +
                                            activityContext.getString(R.string.this_main_asset_issued) + " " +
                                            foundedOperation.getPersonName();
                                }
                            }
                        } else {
                            if (foundedOperation.getTypeOperation() == Operation.TYPE_OPERATION_RETURNING_MAIN_ASSET) {
                                error = activityContext.getString(R.string.main_asset) + " " + operation.getOwnerName() + " " +
                                        activityContext.getString(R.string.this_main_asset_returned);
//                                if (foundedOperation.getPersonID() == operation.getPersonID()) {
//                                    error = activityContext.getString(R.string.main_asset) + " " + operation.getOwnerName() + " " +
//                                            activityContext.getString(R.string.this_main_asset_returned) + " " +
//                                            activityContext.getString(R.string.this_person_1);
//                                } else {
//                                    error = activityContext.getString(R.string.main_asset) + " " + operation.getOwnerName() + " " +
//                                            activityContext.getString(R.string.this_main_asset_returned) + " " +
//                                            foundedOperation.getPersonName();
//                                }
                            }else{
                                if (mainAsset.getPersonID() != person.getId()) {
                                    error = activityContext.getString(R.string.main_asset) + " " + operation.getOwnerName() + " " +
                                            activityContext.getString(R.string.listed_with) + " " +
                                            mainAsset.getPersonName();
                                }
                            }
                        }
                    }else{
                        if (operation.getTypeOperation() == Operation.TYPE_OPERATION_ISSUING_MAIN_ASSET
                                && !TextUtils.isEmpty(mainAsset.getPersonName())){
                            error = activityContext.getString(R.string.main_asset) + " " + operation.getOwnerName() + " " +
                                    activityContext.getString(R.string.this_main_asset_issued) + " " +
                                    mainAsset.getPersonName();
                        }
                        if (operation.getTypeOperation() == Operation.TYPE_OPERATION_RETURNING_MAIN_ASSET
                                && TextUtils.isEmpty(mainAsset.getPersonName())){
                            error = activityContext.getString(R.string.main_asset) + " " + operation.getOwnerName() + " " +
                                    activityContext.getString(R.string.this_main_asset_returned);
                        }
                    }

                    if(!TextUtils.isEmpty(error)) {
                        return new LoadItemEventHandler(LoadItemEventHandler.LOAD_ERROR, error);
                    }

                    dataBase.dataBaseDao().insert(operation);
                    mainAsset.setPersonIDOld(mainAsset.getPersonID());
                    mainAsset.setPersonID(operation.getPersonID());
                    mainAsset.setPersonName(operation.getPersonName());
                    mainAsset.setPersonNameUpper(TextUtils.isEmpty(operation.getPersonName())
                            ? operation.getPersonName() : operation.getPersonName().toUpperCase());
                    mainAsset.setTimeEditPerson(new Date().getTime());
                    dataBase.dataBaseDao().insert(mainAsset);

                    return new LoadItemEventHandler(LoadItemEventHandler.LOAD_FINISH);
                } catch (Exception e) {
                    Log.e(TAGLOG_LOAD, e.toString());
                    return new LoadItemEventHandler(LoadItemEventHandler.LOAD_ERROR);
                }
            }

            @Override
            protected void onPostExecute(LoadItemEventHandler loadItemEventHandler) {
                super.onPostExecute(loadItemEventHandler);
                resultSave.setValue(loadItemEventHandler);
            }

        }.execute();

    }

    public MutableLiveData<MainAsset> getMainAssetByRfid(String rfid) {
        MutableLiveData<MainAsset> result = new MutableLiveData<>();

        new AsyncTask<Void, Void, MainAsset>() {
            @Override
            protected MainAsset doInBackground(Void... booleans) {
                try {
                    String filter = "("+ DBConstant.MAIN_ASSET_RFID_UPPER + " like '%" + rfid.toUpperCase() + "%')";
                    return dataBase.dataBaseDao().getMainAssetByFilter(filter);

                } catch (Exception e) {
                    Log.e(TAGLOG_LOAD, e.toString());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(MainAsset mainAsset) {
                super.onPostExecute(mainAsset);
                result.setValue(mainAsset);
            }

        }.execute();
        return result;
    }

    public MutableLiveData<Person> getPersonByRfid(String rfid) {
        MutableLiveData<Person> result = new MutableLiveData<>();

        new AsyncTask<Void, Void, Person>() {
            @Override
            protected Person doInBackground(Void... booleans) {
                try {
                    String filter = "("+ DBConstant.MAIN_ASSET_RFID_UPPER + " like '%" + rfid.toUpperCase() + "%')";
                    return dataBase.dataBaseDao().getPersonByFilter(filter);

                } catch (Exception e) {
                    Log.e(TAGLOG_LOAD, e.toString());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Person person) {
                super.onPostExecute(person);
                result.setValue(person);
            }

        }.execute();
        return result;
    }

}