package ua.com.expertsolution.chesva.ui.fragments.boxAddRfid;

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
import ua.com.expertsolution.chesva.model.dto.Box;
import ua.com.expertsolution.chesva.model.dto.Operation;
import ua.com.expertsolution.chesva.utils.NumberUtils;

import static ua.com.expertsolution.chesva.adapter.FilterAutoCompleteAdapter.SIZE_FILTER_LIST;
import static ua.com.expertsolution.chesva.common.Consts.DATE_SYNC_FORMAT;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_LOAD;

@SuppressLint("LongLogTag")
public class BoxAddRfidViewModel extends AndroidViewModel {
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
    private List<Box> boxListAll;

    public BoxAddRfidViewModel(@NonNull Application application) {
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
                            .inSearchOf(dataBase.dataBaseDao().getCountListByFilter(DBConstant.BOX_TABLE, null))
                            .found(dataBase.dataBaseDao().getCountListByFilter(DBConstant.BOX_TABLE, DBConstant.BOX_RFID + " IS NOT NULL"))
                            .created(dataBase.dataBaseDao().getCountListByFilter(DBConstant.BOX_TABLE, DBConstant.BOX_RFID + " IS NULL"))
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

    public AsyncTask getBoxList(int startId, int limit, int filterType) {

        resultList.setValue(new LoadListEventHandler.Builder().status(LoadListEventHandler.LOAD_STARTED).build());

        return new AsyncTask<Boolean, LoadListEventHandler, LoadListEventHandler>() {
            @Override
            protected LoadListEventHandler doInBackground(Boolean... booleans) {
                List<Box> boxList = new ArrayList<>();
                try {
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

                    if(boxListAll == null || boxListAll.size()==0 || startId == 0) {
                        boxListAll = dataBase.dataBaseDao().getBoxListByFilter(filter);
                    }
                    if(boxListAll.size()<startId+limit){
                        boxList = boxListAll;
                    }else{
                        boxList = boxListAll.subList(startId,
                                (startId+limit)>boxListAll.size()? boxListAll.size() :startId+limit);
                    }

                } catch (Exception e) {
                    Log.e(TAGLOG_LOAD, e.toString());
                return new LoadListEventHandler.Builder().status(LoadListEventHandler.LOAD_ERROR).build();
                }
                return new LoadListEventHandler.Builder().status(LoadListEventHandler.LOAD_FINISH).boxList(boxList).build();
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


    public AsyncTask saveBox(Box box) {

        resultSave.setValue(new LoadItemEventHandler(LoadItemEventHandler.LOAD_STARTED));

        return new AsyncTask<Void, Void, LoadItemEventHandler>() {
            @Override
            protected LoadItemEventHandler doInBackground(Void... booleans) {
                try {

                    dataBase.dataBaseDao().insert(box);
                    dataBase.dataBaseDao().insert(Operation.builder()
                            .typeOperation(Operation.TYPE_OPERATION_BOX_ADD_RFID)
                            .idOwner(box.getId())
                            .rfid(box.getRfid())
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