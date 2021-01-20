package ua.com.expertsolution.chesva.ui.fragments.mainassetinbox;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
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
import ua.com.expertsolution.chesva.model.dto.Box;
import ua.com.expertsolution.chesva.model.dto.MainAsset;
import ua.com.expertsolution.chesva.model.dto.Operation;
import ua.com.expertsolution.chesva.model.dto.Person;

import static ua.com.expertsolution.chesva.adapter.FilterAutoCompleteAdapter.SIZE_FILTER_LIST;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_LOAD;

@SuppressLint("LongLogTag")
public class MainAssetInBoxViewModel extends AndroidViewModel {

    @Inject
    DataBase dataBase;

    private MutableLiveData<LoadStatusHandler> resultStatus;
    private MutableLiveData<LoadListEventHandler> resultList;
    private MutableLiveData<LoadFiltersHandler> resultFilter;
    private MutableLiveData<LoadListEventHandler> resultFound;
    private MutableLiveData<LoadItemEventHandler> resultSave;
    private List<MainAsset> mainAssetListAll;
    private Context activityContext;

    public MainAssetInBoxViewModel(@NonNull Application application) {
        super(application);
        resultStatus = new MutableLiveData<>();
        resultList = new MutableLiveData<>();
        resultFilter = new MutableLiveData<>();
        resultFound = new MutableLiveData<>();
        resultSave = new MutableLiveData<>();
        ((InventoryApplication) application).getComponent().inject(this);activityContext = application;
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

    public AsyncTask updateStatuses(int typeOperation, int boxID) {

        return new AsyncTask<Void, Void, LoadStatusHandler>() {
            @Override
            protected LoadStatusHandler doInBackground(Void... booleans) {
                try {
                    return LoadStatusHandler.builder()
                            .found(dataBase.dataBaseDao().getCountListByFilter(DBConstant.MAIN_ASSET_TABLE,
                                    (DBConstant.MAIN_ASSET_BOX_ID + "=" + boxID)))
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

    public AsyncTask getMainAssetsList(int startId, int limit, int typeOperation, int boxID) {

        resultList.setValue(new LoadListEventHandler.Builder().status(LoadListEventHandler.LOAD_STARTED).build());

        return new AsyncTask<Boolean, LoadListEventHandler, LoadListEventHandler>() {
            @Override
            protected LoadListEventHandler doInBackground(Boolean... booleans) {
                List<MainAsset> mainAssets = new ArrayList<>();
                try {
                    if(mainAssetListAll == null || mainAssetListAll.size()==0 || startId == 0) {
                        mainAssetListAll = dataBase.dataBaseDao().getMainAssetListByFilter(DBConstant.MAIN_ASSET_BOX_ID + "=" + boxID);
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
                            dataBase.dataBaseDao().getFilterBoxListByColumn(SIZE_FILTER_LIST, column, value.toUpperCase()));
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


    public AsyncTask saveOperation(Operation operation) {

        resultSave.setValue(new LoadItemEventHandler(LoadItemEventHandler.LOAD_STARTED));

        return new AsyncTask<Void, Void, LoadItemEventHandler>() {
            @Override
            protected LoadItemEventHandler doInBackground(Void... booleans) {
                try {

                    String query =  "SELECT * FROM "+DBConstant.OPERATION_TABLE +
                            " WHERE (" + DBConstant.OPERATION_ID_OWNER + "=" + operation.getIdOwner() +
                            " AND " + DBConstant.OPERATION_TYPE_OPERATION + "=" + Operation.TYPE_OPERATION_MAIN_ASSET_IN_BOX + ")"
                            + " ORDER BY " + DBConstant.OPERATION_ID + " DESC LIMIT 1";
                    List<Operation> founded = dataBase.dataBaseDao().getOperationListByQuery(new SimpleSQLiteQuery(query));

                    if (founded != null && founded.size()>0) {
                        Operation foundedOperation = founded.get(0);
                        String error = "";
                        if (operation.getBoxID() == -1 && operation.getBoxID()== foundedOperation.getBoxID()) {
                            error = activityContext.getString(R.string.main_asset) + " " + operation.getOwnerName() + " " +
                                    activityContext.getString(R.string.already_withdrawn);
                        }else if(operation.getBoxID() > -1 && foundedOperation.getBoxID() > -1) {
                            if (foundedOperation.getBoxID() == operation.getBoxID()) {
                                error = activityContext.getString(R.string.main_asset) + " " + operation.getOwnerName() + " " +
                                        activityContext.getString(R.string.posted_in) + " " +
                                        activityContext.getString(R.string.this_box);
                            } else {
                                error = activityContext.getString(R.string.main_asset) + " " + operation.getOwnerName() + " " +
                                        activityContext.getString(R.string.posted_in) + " " +
                                        foundedOperation.getBoxName();
                            }
                        }

                       if(!TextUtils.isEmpty(error)) {
                            return new LoadItemEventHandler(LoadItemEventHandler.LOAD_ERROR, error);
                        }
                    }

                    dataBase.dataBaseDao().insert(operation);
                    MainAsset mainAsset = dataBase.dataBaseDao().getMainAssetById(operation.getIdOwner());
                    mainAsset.setBoxID(operation.getBoxID());
                    mainAsset.setBoxName(operation.getBoxName());
                    mainAsset.setBoxNameUpper(TextUtils.isEmpty(operation.getBoxName())
                            ? operation.getBoxName() : operation.getBoxName().toUpperCase());
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

    public MutableLiveData<Box> getBoxByRfid(String rfid) {
        MutableLiveData<Box> result = new MutableLiveData<>();

        new AsyncTask<Void, Void, Box>() {
            @Override
            protected Box doInBackground(Void... booleans) {
                try {
                    String filter = "("+ DBConstant.BOX_RFID_UPPER + " like '%" + rfid.toUpperCase() + "%')";
                    return dataBase.dataBaseDao().getBoxByFilter(filter);

                } catch (Exception e) {
                    Log.e(TAGLOG_LOAD, e.toString());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Box box) {
                super.onPostExecute(box);
                result.setValue(box);
            }

        }.execute();
        return result;
    }

}