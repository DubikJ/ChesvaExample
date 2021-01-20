package ua.com.expertsolution.chesva.modules;

import android.app.Application;

import javax.inject.Singleton;

import androidx.room.Room;
import dagger.Module;
import dagger.Provides;
import ua.com.expertsolution.chesva.db.DBConstant;
import ua.com.expertsolution.chesva.db.DataBase;

@Module
public class DataBaseApiModule {

    private Application application;

    public DataBaseApiModule(Application application) {
        this.application = application;
    }

    @Singleton
    @Provides
    public Application provideContext(){
        return application;
    }

    @Provides
    @Singleton
    public DataBase getDataRepository() {
        return Room.databaseBuilder(application.getBaseContext(), DataBase.class, DBConstant.NAME_DATA_BASE_ROOM)
                .fallbackToDestructiveMigration()
                .build();
    }


}
