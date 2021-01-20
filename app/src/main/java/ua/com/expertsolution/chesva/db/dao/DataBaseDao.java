package ua.com.expertsolution.chesva.db.dao;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.text.TextUtils;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;
import io.reactivex.Single;
import ua.com.expertsolution.chesva.common.Consts;
import ua.com.expertsolution.chesva.db.DBConstant;
import ua.com.expertsolution.chesva.model.dto.Box;
import ua.com.expertsolution.chesva.model.dto.Condition;
import ua.com.expertsolution.chesva.model.dto.MainAsset;
import ua.com.expertsolution.chesva.model.dto.Operation;
import ua.com.expertsolution.chesva.model.dto.Person;
import ua.com.expertsolution.chesva.utils.SharedStorage;

@Dao
public abstract class  DataBaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(Box box);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(MainAsset mainAsset);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(Condition condition);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(Person person);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(Operation operation);

    @Update(onConflict = OnConflictStrategy.FAIL)
    public abstract int update(Box box);

    @Query("UPDATE " + DBConstant.OPERATION_TABLE + " SET " + DBConstant.OPERATION_SEND + " =0 WHERE " + DBConstant.OPERATION_ID + " =:id ")
    public abstract void updateOperationSendById(int id);

    @Query("SELECT * FROM " + DBConstant.MAIN_ASSET_TABLE + " WHERE " + DBConstant.MAIN_ASSET_ID + " =:id ")
    public abstract MainAsset getMainAssetById(int id);

    @Update(onConflict = OnConflictStrategy.FAIL)
    public abstract int update(MainAsset mainAsset);

    @Update(onConflict = OnConflictStrategy.FAIL)
    public abstract int update(Condition condition);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertBoxList(List<Box> boxList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertMainAssetList(List<MainAsset> mainAssetList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertConditionList(List<Condition> conditionList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPersonList(List<Person> personList);

    @Update(onConflict = OnConflictStrategy.FAIL)
    abstract void updateBoxList(List<Box> boxList);

    @Update(onConflict = OnConflictStrategy.FAIL)
    abstract void updateMainAssetList(List<MainAsset> mainAssetList);

    @Update(onConflict = OnConflictStrategy.FAIL)
    abstract void updateConditionList(List<Condition> conditionList);

    @Insert(onConflict = OnConflictStrategy.FAIL)
    public abstract void updatePersonList(List<Person> personList);

    @Transaction
    @RawQuery
    abstract int getCountListByQuery(SupportSQLiteQuery supportSQLiteQuery);

    @Transaction
    @RawQuery
    abstract List<String> getFilterListByColumn(SupportSQLiteQuery supportSQLiteQuery);

    @Transaction
    @RawQuery
    abstract Box getBoxByQuery(SupportSQLiteQuery supportSQLiteQuery);

    @Transaction
    @RawQuery
    abstract List<Box> getBoxListByQuery(SupportSQLiteQuery supportSQLiteQuery);

    @Transaction
    @Query("SELECT * FROM "+ DBConstant.OPERATION_TABLE +
            " WHERE (" + DBConstant.OPERATION_TYPE_OPERATION + " =:typeOperation  AND "
            + DBConstant.OPERATION_ID + " >=:startId  AND "
            + DBConstant.OPERATION_SEND + "=1)"+
            " LIMIT :limit ")
    public abstract Single<List<Operation>> getUploadOperationList(int typeOperation, int startId, int limit);

    @Transaction
    @RawQuery
    abstract MainAsset getMainAssetByQuery(SupportSQLiteQuery supportSQLiteQuery);

    @Transaction
    @RawQuery
    abstract Person getPersonByQuery(SupportSQLiteQuery supportSQLiteQuery);

    @Transaction
    @RawQuery
    abstract List<MainAsset> getMainAssetListByQuery(SupportSQLiteQuery supportSQLiteQuery);

    @Transaction
    @RawQuery
    abstract List<Person> getPersonListByQuery(SupportSQLiteQuery supportSQLiteQuery);

    @Transaction
    @RawQuery
    public abstract List<Operation> getOperationListByQuery(SupportSQLiteQuery supportSQLiteQuery);

    @Query("DELETE FROM "+ DBConstant.BOX_TABLE)
    public abstract void deleteBox();

    @Query("DELETE FROM "+ DBConstant.MAIN_ASSET_TABLE)
    public abstract void deleteMainAsset();

    @Query("DELETE FROM "+ DBConstant.CONDITION_TABLE)
    public abstract void deleteCondition();

    @Query("DELETE FROM "+ DBConstant.PERSON_TABLE)
    public abstract void deletePerson();

    @Query("DELETE FROM "+ DBConstant.OPERATION_TABLE)
    public abstract void deleteOperation();

    public void clearDB(Context context) {
        deleteBox();
        deleteMainAsset();
        deleteCondition();
        deletePerson();
        deleteOperation();
        SharedStorage.setBoolean(context, Consts.APP_SETTINGS_PREFS, Consts.IS_DB_NO_EMPTY, false);
    }

    public void insertListBox(Context context, List<Box> boxList) {
        try {
            insertBoxList(boxList);
        } catch (SQLiteConstraintException exception) {
            updateBoxList(boxList);
        }

    }

    public void insertMainAssetList(Context context, List<MainAsset> mainAssetList) {
        try {
            insertMainAssetList(mainAssetList);
        } catch (SQLiteConstraintException exception) {
            updateMainAssetList(mainAssetList);
        }
    }

    public void insertConditionlist(Context context, List<Condition> conditionList) {
        try {
            insertConditionList(conditionList);
        } catch (SQLiteConstraintException exception) {
            updateConditionList(conditionList);
        }
    }

    public int getCountListByFilter(String nameTable, String filter) {
        String query =  "SELECT COUNT (*) FROM "+ nameTable + (TextUtils.isEmpty(filter)? "" : " WHERE (" + filter + ")");
        return getCountListByQuery(new SimpleSQLiteQuery(query));
    }

    public Box getBoxByFilter(String filter) {
        String query =  "SELECT * FROM "+DBConstant.BOX_TABLE + " WHERE (" + filter + ")";
        return getBoxByQuery(new SimpleSQLiteQuery(query));
    }

    public MainAsset getMainAssetByFilter(String filter) {
        String query =  "SELECT * FROM "+DBConstant.MAIN_ASSET_TABLE + " WHERE (" + filter + ")";
        return getMainAssetByQuery(new SimpleSQLiteQuery(query));
    }

    public Person getPersonByFilter(String filter) {
        String query =  "SELECT * FROM "+DBConstant.PERSON_TABLE + " WHERE (" + filter + ")";
        return getPersonByQuery(new SimpleSQLiteQuery(query));
    }

    public List<Box> getBoxListByFilter(String filter) {
        String query =  "SELECT * FROM "+DBConstant.BOX_TABLE + (TextUtils.isEmpty(filter)? "" : " WHERE (" + filter + ")")
                + " ORDER BY " + DBConstant.BOX_NAME + " ASC ";
        return getBoxListByQuery(new SimpleSQLiteQuery(query));
    }

    public List<MainAsset> getMainAssetListByFilter(String filter) {
        String query =  "SELECT * FROM "+DBConstant.MAIN_ASSET_TABLE + (TextUtils.isEmpty(filter)? "" : " WHERE (" + filter + ")")
                + " ORDER BY " + DBConstant.MAIN_ASSET_NAME + " ASC ";
        return getMainAssetListByQuery(new SimpleSQLiteQuery(query));
    }

    public List<MainAsset> getMainAssetListByQuery(String query) {
        return getMainAssetListByQuery(new SimpleSQLiteQuery(query));
    }

    public List<Operation> getOperationListByFilter(String filter) {
        String query =  "SELECT * FROM "+DBConstant.OPERATION_TABLE + (TextUtils.isEmpty(filter)? "" : " WHERE (" + filter + ")")
                + " ORDER BY " + DBConstant.MAIN_ASSET_ID + " ASC ";
        return getOperationListByQuery(new SimpleSQLiteQuery(query));
    }

    public List<String> getFilterListByColumn(int limit, String nameTable, String column, String value) {
        String query =  "SELECT DISTINCT CAST(" + column + " AS TEXT) FROM "+nameTable
                + " WHERE (" + column + "_upper like '%" +value+ "%')"+
                " ORDER BY " + column + " ASC " +
                (limit > 0 ? " LIMIT "+limit : "") ;
        return getFilterListByColumn(new SimpleSQLiteQuery(query));
    }

    public List<Box> getFilterBoxListByColumn(int limit, String column, String value) {
        String query =  "SELECT * FROM "+ DBConstant.BOX_TABLE
                + " WHERE (" + column + "_upper like '%" +value+ "%')"+
                " ORDER BY " + column + " ASC " +
                (limit > 0 ? " LIMIT "+limit : "") ;
        return getBoxListByQuery(new SimpleSQLiteQuery(query));
    }

    public List<MainAsset> getFilterMainAssetListByColumn(int limit, String column, String value) {
        String query =  "SELECT * FROM "+ DBConstant.MAIN_ASSET_TABLE
                + " WHERE (" + column + "_upper like '%" +value+ "%')"+
                " ORDER BY " + column + " ASC " +
                (limit > 0 ? " LIMIT "+limit : "") ;
        return getMainAssetListByQuery(new SimpleSQLiteQuery(query));
    }

    public List<Person> getFilterPersonListByColumn(int limit, String column, String value) {
        String query =  "SELECT * FROM "+ DBConstant.PERSON_TABLE
                + " WHERE (" + column + "_upper like '%" +value+ "%')"+
                " ORDER BY " + column + " ASC " +
                (limit > 0 ? " LIMIT "+limit : "") ;
        return getPersonListByQuery(new SimpleSQLiteQuery(query));
    }

    public void setOperationListSended(List<Operation> operationList){
        for(Operation operation: operationList){
            updateOperationSendById(operation.getId());
        }
    }

}