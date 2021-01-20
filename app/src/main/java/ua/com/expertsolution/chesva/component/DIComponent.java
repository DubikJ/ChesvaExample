package ua.com.expertsolution.chesva.component;

import javax.inject.Singleton;

import dagger.Component;
import ua.com.expertsolution.chesva.modules.DataBaseApiModule;
import ua.com.expertsolution.chesva.modules.SyncServiceApiModule;
import ua.com.expertsolution.chesva.ui.activities.MainActivity;
import ua.com.expertsolution.chesva.ui.fragments.boxAddRfid.BoxAddRfidViewModel;
import ua.com.expertsolution.chesva.ui.fragments.download.DownloadViewModel;
import ua.com.expertsolution.chesva.ui.fragments.geiger.GeigerViewModel;
import ua.com.expertsolution.chesva.ui.fragments.issuingreturning.IssuingReturningViewModel;
import ua.com.expertsolution.chesva.ui.fragments.login.LoginViewModel;
import ua.com.expertsolution.chesva.ui.fragments.mainAssetAddRfid.MainAssetAddRfidViewModel;
import ua.com.expertsolution.chesva.ui.fragments.mainassetinbox.MainAssetInBoxViewModel;
import ua.com.expertsolution.chesva.ui.fragments.searchmainasset.SearchMainAssetViewModel;
import ua.com.expertsolution.chesva.ui.fragments.settings.SettingsFragment;
import ua.com.expertsolution.chesva.ui.fragments.upload.UploadViewModel;

@Singleton
@Component(modules = {
        DataBaseApiModule.class, SyncServiceApiModule.class})
public interface DIComponent {
    void inject(MainActivity mainActivity);
    void inject(DownloadViewModel DownloadViewModel);
    void inject(UploadViewModel uploadViewModel);
    void inject(BoxAddRfidViewModel boxAddRfidViewModel);
    void inject(MainAssetAddRfidViewModel mainAssetAddRfidViewModel);
    void inject(GeigerViewModel geigerViewModel);
    void inject(LoginViewModel loginViewModel);
    void inject(SettingsFragment settingsFragment);
    void inject(IssuingReturningViewModel issuingReturningViewModel);
    void inject(MainAssetInBoxViewModel mainAssetInBoxViewModel);
    void inject(SearchMainAssetViewModel searchMainAssetViewModel);
}
