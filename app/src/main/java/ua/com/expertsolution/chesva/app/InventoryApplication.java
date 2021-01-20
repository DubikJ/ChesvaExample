package ua.com.expertsolution.chesva.app;


import androidx.multidex.MultiDexApplication;
import ua.com.expertsolution.chesva.component.DIComponent;
import ua.com.expertsolution.chesva.component.DaggerDIComponent;
import ua.com.expertsolution.chesva.modules.DataBaseApiModule;
import ua.com.expertsolution.chesva.modules.SyncServiceApiModule;

public class InventoryApplication extends MultiDexApplication {

    DIComponent diComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        diComponent = DaggerDIComponent.builder()
                .dataBaseApiModule(new DataBaseApiModule(this))
                .syncServiceApiModule(new SyncServiceApiModule(this))
                .build();
    }

    public DIComponent getComponent() {
        return diComponent;
    }
}
