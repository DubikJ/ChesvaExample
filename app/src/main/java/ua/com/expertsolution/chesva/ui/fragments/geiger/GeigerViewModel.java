package ua.com.expertsolution.chesva.ui.fragments.geiger;

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
import ua.com.expertsolution.chesva.model.LoadListEventHandler;
import ua.com.expertsolution.chesva.model.db.Facility;

import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_LOAD;

@SuppressLint("LongLogTag")
public class GeigerViewModel extends AndroidViewModel {

    @Inject
    DataBase dataBase;

    private MutableLiveData<LoadListEventHandler> resultSync;

    public GeigerViewModel(@NonNull Application application) {
        super(application);
        resultSync = new MutableLiveData<>();
        ((InventoryApplication) application).getComponent().inject(this);
    }

    public LiveData<LoadListEventHandler> getResultLoad() {
        return resultSync;
    }

    public AsyncTask getFacilityList(int startId, int limit, String name, String rfid) {

        resultSync.setValue(new LoadListEventHandler.Builder().status(LoadListEventHandler.LOAD_STARTED).build());

        return new AsyncTask<Boolean, LoadListEventHandler, LoadListEventHandler>() {
            @Override
            protected LoadListEventHandler doInBackground(Boolean... booleans) {
//                List<Facility> facilityList = new ArrayList<>();
//                try {
//
//                    String filter = "";
//                    if (!TextUtils.isEmpty(rfid)) {
//                        filter += "("+ DBConstant.FACILITY_RFID_UPPER + " like '%" + rfid.toUpperCase() + "%')";
//                    }
//
//                    if (!TextUtils.isEmpty(name)) {
//                        if (!TextUtils.isEmpty(filter)) {
//                            filter += " AND ";
//                        }
//                        filter += "("+DBConstant.FACILITY_NAME_UPPER + " like '%" + name.toUpperCase() + "%')";
//                    }
//
//                    facilityList = dataBase.dataBaseDao().getFacilityListByCountFilter(startId, limit, filter);
//
//                } catch (Exception e) {
//                    Log.e(TAGLOG_LOAD, e.toString());
                    return new LoadListEventHandler.Builder().status(LoadListEventHandler.LOAD_ERROR).build();
//                }
//                return new LoadListEventHandler(LoadListEventHandler.LOAD_FINISH, facilityList);
            }

            @Override
            protected void onPostExecute(LoadListEventHandler LoadListEventHandler) {
                super.onPostExecute(LoadListEventHandler);
                resultSync.setValue(LoadListEventHandler);
            }

        }.execute();

    }

}