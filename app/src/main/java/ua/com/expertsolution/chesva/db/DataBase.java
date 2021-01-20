package ua.com.expertsolution.chesva.db;

import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import ua.com.expertsolution.chesva.db.dao.DataBaseDao;
import ua.com.expertsolution.chesva.model.dto.Box;
import ua.com.expertsolution.chesva.model.dto.Condition;
import ua.com.expertsolution.chesva.model.dto.MainAsset;
import ua.com.expertsolution.chesva.model.dto.Operation;
import ua.com.expertsolution.chesva.model.dto.Person;

@Database(entities = {Box.class, MainAsset.class, Condition.class, Person.class, Operation.class},
        version = DBConstant.DATA_BASE_VERSION, exportSchema = false)
public abstract class DataBase extends RoomDatabase {
    public abstract DataBaseDao dataBaseDao();

    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }
}
