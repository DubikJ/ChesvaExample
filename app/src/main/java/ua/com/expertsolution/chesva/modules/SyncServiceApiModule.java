package ua.com.expertsolution.chesva.modules;


import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ua.com.expertsolution.chesva.service.sync.SyncServiceApi;

@Module
public class SyncServiceApiModule {

    private Application application;

    public SyncServiceApiModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public SyncServiceApi getSyncQuery() {

        return new SyncServiceApi(application);
    }
}
