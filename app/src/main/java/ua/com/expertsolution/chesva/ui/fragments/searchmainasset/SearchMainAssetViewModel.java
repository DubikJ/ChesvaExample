package ua.com.expertsolution.chesva.ui.fragments.searchmainasset;

import android.annotation.SuppressLint;
import android.app.Application;
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
import ua.com.expertsolution.chesva.app.InventoryApplication;
import ua.com.expertsolution.chesva.db.DBConstant;
import ua.com.expertsolution.chesva.db.DataBase;
import ua.com.expertsolution.chesva.model.LoadFiltersHandler;
import ua.com.expertsolution.chesva.model.LoadListEventHandler;
import ua.com.expertsolution.chesva.model.dto.MainAsset;

import static ua.com.expertsolution.chesva.adapter.FilterAutoCompleteAdapter.SIZE_FILTER_LIST;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_LOAD;

@SuppressLint("LongLogTag")
public class SearchMainAssetViewModel extends AndroidViewModel {

    @Inject
    DataBase dataBase;

    private MutableLiveData<LoadListEventHandler> resultList;
    private MutableLiveData<LoadFiltersHandler> resultFilter;
    private List<MainAsset> mainAssetListAll;

    public SearchMainAssetViewModel(@NonNull Application application) {
        super(application);
        resultList = new MutableLiveData<>();
        resultFilter = new MutableLiveData<>();
        ((InventoryApplication) application).getComponent().inject(this);
    }

    public LiveData<LoadListEventHandler> getResultList() {
        return resultList;
    }

    public LiveData<LoadFiltersHandler> getResultFilter() {
        return resultFilter;
    }


    public AsyncTask getMainAssetList(int startId, int limit, String personName, String modelName, String name, String boxName) {

        resultList.setValue(new LoadListEventHandler.Builder().status(LoadListEventHandler.LOAD_STARTED).build());

        return new AsyncTask<Boolean, LoadListEventHandler, LoadListEventHandler>() {
            @Override
            protected LoadListEventHandler doInBackground(Boolean... booleans) {
                List<MainAsset> mainAssets = new ArrayList<>();
                try {

                    if(mainAssetListAll == null || mainAssetListAll.size()==0 || startId == 0) {
                        String filter = "";
                        if(!TextUtils.isEmpty(personName)){
                            filter = filter + DBConstant.MAIN_ASSET_PERSON_NAME + "_upper like '%" +personName+ "%'";
                        }
                        if(!TextUtils.isEmpty(modelName)){
                            if(!TextUtils.isEmpty(filter)){
                                filter = filter + " AND ";
                            }
                            filter = filter + DBConstant.MAIN_ASSET_MODEL_NAME + "_upper like '%" +modelName+ "%'";
                        }
                        if(!TextUtils.isEmpty(name)){
                            if(!TextUtils.isEmpty(filter)){
                                filter = filter + " AND ";
                            }
                            filter = filter + DBConstant.MAIN_ASSET_NAME + "_upper like '%" +name+ "%'";
                        }
                        if(!TextUtils.isEmpty(boxName)){
                            if(!TextUtils.isEmpty(filter)){
                                filter = filter + " AND ";
                            }
                            filter = filter + DBConstant.MAIN_ASSET_BOX_NAME + "_upper like '%" +boxName+ "%'";
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

    public AsyncTask loadFilters(String column, String value) {

        return new AsyncTask<Void, Void, LoadFiltersHandler>() {
            @Override
            protected LoadFiltersHandler doInBackground(Void... booleans) {
                try {
                    return new LoadFiltersHandler(column,
                            dataBase.dataBaseDao().getFilterListByColumn(SIZE_FILTER_LIST,
                                    DBConstant.MAIN_ASSET_TABLE, column, value.toUpperCase()));
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

}