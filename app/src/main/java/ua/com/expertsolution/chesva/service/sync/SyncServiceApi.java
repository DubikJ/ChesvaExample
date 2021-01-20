package ua.com.expertsolution.chesva.service.sync;

import android.content.Context;

public class SyncServiceApi {

    private Context mContext;
    private SyncService syncService;

    public SyncServiceApi(Context mContext) {
        this.mContext = mContext;
        this.syncService = SyncServiceFactory.createService(
                SyncService.class, mContext);
    }

    public SyncServiceApi(Context mContext, SyncService syncService) {
        this.mContext = mContext;
        this.syncService = syncService;
    }

    public void rebuildService(){
        this.syncService = SyncServiceFactory.createService(
                SyncService.class, mContext);
    }

    public SyncService getSyncService() {
        return syncService;
    }
}
