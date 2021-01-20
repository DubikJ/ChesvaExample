package ua.com.expertsolution.chesva.ui.fragments.mainAssetAddRfid;

import android.annotation.SuppressLint;
import android.app.Application;
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
import ua.com.expertsolution.chesva.app.InventoryApplication;
import ua.com.expertsolution.chesva.db.DBConstant;
import ua.com.expertsolution.chesva.db.DataBase;
import ua.com.expertsolution.chesva.model.LoadFiltersHandler;
import ua.com.expertsolution.chesva.model.LoadItemEventHandler;
import ua.com.expertsolution.chesva.model.LoadListEventHandler;
import ua.com.expertsolution.chesva.model.LoadStatusHandler;
import ua.com.expertsolution.chesva.model.dto.MainAsset;
import ua.com.expertsolution.chesva.model.dto.Operation;
import ua.com.expertsolution.chesva.utils.NumberUtils;

import static ua.com.expertsolution.chesva.adapter.FilterAutoCompleteAdapter.SIZE_FILTER_LIST;
import static ua.com.expertsolution.chesva.common.Consts.DATE_SYNC_FORMAT;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_LOAD;

@SuppressLint("LongLogTag")
public class MainAssetAddRfidViewModel extends AndroidViewModel {
    public static final int TYPE_FILTER_LIST_ALL = 0;
    public static final int TYPE_FILTER_LIST_WITH_RFID = 1;
    public static final int TYPE_FILTER_LIST_WITHOUT_RFID = 2;

    @Inject
    DataBase dataBase;

    private MutableLiveData<LoadStatusHandler> resultStatus;
    private MutableLiveData<LoadListEventHandler> resultList;
    private MutableLiveData<LoadFiltersHandler> resultFilter;
    private MutableLiveData<LoadListEventHandler> resultFound;
    private MutableLiveData<LoadItemEventHandler> resultSave;
    private List<MainAsset> mainAssetListAll;

    public MainAssetAddRfidViewModel(@NonNull Application application) {
        super(application);
        resultStatus = new MutableLiveData<>();
        resultList = new MutableLiveData<>();
        resultFilter = new MutableLiveData<>();
        resultFound = new MutableLiveData<>();
        resultSave = new MutableLiveData<>();
        ((InventoryApplication) application).getComponent().inject(this);
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


    public AsyncTask updateStatuses() {

        return new AsyncTask<Void, Void, LoadStatusHandler>() {
            @Override
            protected LoadStatusHandler doInBackground(Void... booleans) {
                try {
                    return LoadStatusHandler.builder()
                            .inSearchOf(dataBase.dataBaseDao().getCountListByFilter(DBConstant.MAIN_ASSET_TABLE, null))
                            .found(dataBase.dataBaseDao().getCountListByFilter(DBConstant.MAIN_ASSET_TABLE, DBConstant.MAIN_ASSET_RFID + " IS NOT NULL"))
                            .created(dataBase.dataBaseDao().getCountListByFilter(DBConstant.MAIN_ASSET_TABLE, DBConstant.MAIN_ASSET_RFID + " IS NULL"))
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

    public AsyncTask getMainAssetList(int startId, int limit, int filterType) {

        resultList.setValue(new LoadListEventHandler.Builder().status(LoadListEventHandler.LOAD_STARTED).build());

        return new AsyncTask<Boolean, LoadListEventHandler, LoadListEventHandler>() {
            @Override
            protected LoadListEventHandler doInBackground(Boolean... booleans) {
                List<MainAsset> mainAssets = new ArrayList<>();
                try {
                    if(mainAssetListAll == null || mainAssetListAll.size()==0 || startId == 0) {
                        String filter = "";
                        switch (filterType){
                            case TYPE_FILTER_LIST_WITH_RFID:
                                filter = DBConstant.BOX_RFID + " IS NOT NULL";
                                break;
                            case TYPE_FILTER_LIST_WITHOUT_RFID:
                                filter = DBConstant.BOX_RFID + " IS NULL";
                                break;
                            default:
                        }
                        mainAssetListAll = dataBase.dataBaseDao().getMainAssetListByFilter(filter);
                    }
                    if(mainAssetListAll.size()<limit){
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

    public AsyncTask loadFilters(String column, String fromColumn, String modelName, String name, String conditionName) {

        return new AsyncTask<Void, Void, LoadFiltersHandler>() {
            @Override
            protected LoadFiltersHandler doInBackground(Void... booleans) {
                try {

                    String filter = "";
                    if (!TextUtils.isEmpty(modelName)) {
                        if(column.equals(fromColumn) && column.equals(DBConstant.MAIN_ASSET_MODEL_NAME)) {
                            filter += "(" + DBConstant.MAIN_ASSET_MODEL_NAME_UPPER + " like '%" + modelName.toUpperCase() + "%')";
                        }else{
                            filter += "(" + DBConstant.MAIN_ASSET_MODEL_NAME_UPPER + " = '" + modelName.toUpperCase() + "')";
                        }
                    }

                    if (!TextUtils.isEmpty(name)) {
                        if (!TextUtils.isEmpty(filter)) {
                            filter += " AND ";
                        }
                        if(column.equals(fromColumn) && column.equals(DBConstant.MAIN_ASSET_NAME)) {
                            filter += "(" + DBConstant.MAIN_ASSET_NAME_UPPER + " like '%" + name.toUpperCase() + "%')";
                        }else{
                            filter += "(" + DBConstant.MAIN_ASSET_NAME_UPPER + " = '" + name.toUpperCase() + "')";
                        }
                    }

                    if (!TextUtils.isEmpty(conditionName)) {
                        if (!TextUtils.isEmpty(filter)) {
                            filter += " AND ";
                        }
                        if(column.equals(fromColumn) && column.equals(DBConstant.MAIN_ASSET_CONDITION_NAME)) {
                            filter += "(" + DBConstant.MAIN_ASSET_CONDITION_NAME_UPPER + " like '%" + conditionName.toUpperCase() + "%')";
                        }else{
                            filter += "(" + DBConstant.MAIN_ASSET_CONDITION_NAME_UPPER + " = '" + conditionName.toUpperCase() + "')";
                        }
                    }

                    String query =  "SELECT * FROM "+ DBConstant.MAIN_ASSET_TABLE
                            + " WHERE (" + filter + ")"+
                            " ORDER BY " + column + " ASC " +
                            " LIMIT "+SIZE_FILTER_LIST;

                    return new LoadFiltersHandler(column, dataBase.dataBaseDao().getMainAssetListByQuery(query));
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

    public AsyncTask saveMainAsset(MainAsset mainAsset) {

        resultSave.setValue(new LoadItemEventHandler(LoadItemEventHandler.LOAD_STARTED));

        return new AsyncTask<Void, Void, LoadItemEventHandler>() {
            @Override
            protected LoadItemEventHandler doInBackground(Void... booleans) {
                try {

                    dataBase.dataBaseDao().insert(mainAsset);
                    dataBase.dataBaseDao().insert(Operation.builder()
                            .typeOperation(Operation.TYPE_OPERATION_MAIN_ASSET_ADD_RFID)
                            .idOwner(mainAsset.getId())
                            .rfid(mainAsset.getRfid())
                            .conditionID(mainAsset.getConditionID())
                            .comment(mainAsset.getComment())
                            .modelName(mainAsset.getModelName())
                            .edited(DATE_SYNC_FORMAT.format(new Date()))
                            .timeEdit(new Date().getTime())
                            .tempId(NumberUtils.generateUniqueId())
                            .send(1).build());

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
                    String filter = "("+ DBConstant.PERSON_RFID_UPPER + " like '%" + rfid.toUpperCase() + "%')";
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

    public AsyncTask foundMainAsset(String modelName, String name, String conditionName) {

        resultFound.setValue(new LoadListEventHandler.Builder().status(LoadListEventHandler.LOAD_STARTED).build());

        return new AsyncTask<Void, Void, LoadListEventHandler>() {
            @Override
            protected LoadListEventHandler doInBackground(Void... booleans) {
                List<MainAsset> mainAssets = new ArrayList<>();
                try {

                    String filter = "";
                    if (!TextUtils.isEmpty(modelName)) {
                        filter += "("+ DBConstant.MAIN_ASSET_MODEL_NAME_UPPER + " like '%" + modelName.toUpperCase() + "%')";
                    }

                    if (!TextUtils.isEmpty(name)) {
                        if (!TextUtils.isEmpty(filter)) {
                            filter += " AND ";
                        }
                        filter += "("+DBConstant.MAIN_ASSET_NAME_UPPER + " like '%" + name.toUpperCase() + "%')";
                    }

                    if (!TextUtils.isEmpty(conditionName)) {
                        if (!TextUtils.isEmpty(filter)) {
                            filter += " AND ";
                        }
                        filter += "("+DBConstant.MAIN_ASSET_CONDITION_NAME_UPPER + " like '%" + conditionName.toUpperCase() + "%')";
                    }

                    mainAssets = dataBase.dataBaseDao().getMainAssetListByFilter(filter);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAGLOG_LOAD, e.toString());
                    return new LoadListEventHandler.Builder().status(LoadListEventHandler.LOAD_ERROR).build();
                }
                return new LoadListEventHandler.Builder()
                        .status(LoadListEventHandler.LOAD_FINISH)
                        .mainAssets(mainAssets).build();
            }

            @Override
            protected void onPostExecute(LoadListEventHandler LoadListEventHandler) {
                super.onPostExecute(LoadListEventHandler);
                resultFound.setValue(LoadListEventHandler);
            }

        }.execute();

    }

}