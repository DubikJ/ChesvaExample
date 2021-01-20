package ua.com.expertsolution.chesva.ui.fragments.settings;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.adapter.LanguageAdapter;
import ua.com.expertsolution.chesva.app.InventoryApplication;
import ua.com.expertsolution.chesva.model.OperatedLanguage;
import ua.com.expertsolution.chesva.service.sync.SyncServiceApi;
import ua.com.expertsolution.chesva.ui.activities.MainActivity;
import ua.com.expertsolution.chesva.ui.activities.SplashActivity;
import ua.com.expertsolution.chesva.ui.fragments.FragmentOnBackPressed;
import ua.com.expertsolution.chesva.utils.ActivityUtils;
import ua.com.expertsolution.chesva.utils.SharedStorage;

import static ua.com.expertsolution.chesva.common.Consts.APP_SETTINGS_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.PERIOD_LICENSE;
import static ua.com.expertsolution.chesva.common.Consts.SERVER;
import static ua.com.expertsolution.chesva.common.Consts.TYPE_CONNECTION;
import static ua.com.expertsolution.chesva.common.Consts.TYPE_CONNECTION_HTTP;
import static ua.com.expertsolution.chesva.common.Consts.TYPE_CONNECTION_HTTPS;
import static ua.com.expertsolution.chesva.common.Consts.UHF_POWER;
import static ua.com.expertsolution.chesva.common.Consts.UHF_POWER_MAX;
import static ua.com.expertsolution.chesva.common.Consts.UHF_POWER_MIN;
import static ua.com.expertsolution.chesva.common.Consts.UI_LANG;

public class SettingsFragment extends Fragment implements View.OnClickListener, FragmentOnBackPressed {
    private static final String FROM_LOGIN = "from_login";

    @BindView(R.id.type_con)
    EditText etTypeCon;

    @BindView(R.id.server)
    EditText etServer;

    @BindView(R.id.language)
    EditText etLanguage;

    @BindView(R.id.tvLicense)
    TextView tvLicense;

    @BindView(R.id.seekPowerScanner)
    IndicatorSeekBar sbPowerRfid;

    @BindView(R.id.rfid_standard_layout)
    TextInputLayout etRfidStandardLayout;

    @BindView(R.id.rfid_standard)
    EditText etRfidStandard;

    @BindView(R.id.btn_save)
    Button btnSave;

    @Inject
    SyncServiceApi serviceApi;

    private View view;
    private int sbProgress = 0;
//    private List<String> rfidStandardList;
    private Boolean fromLogin = false;

    public static SettingsFragment getInstance(Boolean fromLogin) {
        Bundle args = new Bundle();
        args.putBoolean(FROM_LOGIN, fromLogin);
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return  fragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            fromLogin = bundle.getBoolean(FROM_LOGIN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        readBundle(getArguments());
        ((InventoryApplication) getActivity().getApplication()).getComponent().inject(this);

        setHasOptionsMenu(true);
        if(fromLogin){
            ((SplashActivity) getActivity()).changeToolbar(true, getActivity().getString(R.string.action_settings));
        }else {
            ((MainActivity) getActivity()).changeToolbar(getActivity().getString(R.string.action_settings),
                    false, true, true);
            ((MainActivity) getActivity()).setFragmentRfidCallBack(null);
        }

        btnSave.setOnClickListener(this);
        etRfidStandard.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvLicense.setText(getString(R.string.license_available) + " " +
                new SimpleDateFormat("yyyy MM dd hh:mm")
                        .format(new Date(SharedStorage.getLong(getActivity(), APP_SETTINGS_PREFS, PERIOD_LICENSE, 0))));

        etServer.setText(SharedStorage.getString(getActivity(), APP_SETTINGS_PREFS, SERVER, ""));

        int savedPwrMax = SharedStorage.getInteger(getActivity(), APP_SETTINGS_PREFS, UHF_POWER_MAX, 30);
        int savedPwr = SharedStorage.getInteger(getActivity(), APP_SETTINGS_PREFS, UHF_POWER, 0);
        int savedPwrMin = SharedStorage.getInteger(getActivity(), APP_SETTINGS_PREFS, UHF_POWER_MIN, 0);
        if (savedPwr < savedPwrMin) savedPwr = savedPwrMin;

        sbPowerRfid.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                sbProgress = Integer.valueOf((seekParams.progress * savedPwrMax)/100);
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                if (sbProgress < 5) {
                    sbProgress = 5;
                    seekBar.setProgress(Integer.valueOf((sbProgress * 100) / savedPwrMax));
                } else {
                    if(fromLogin) {
                        SharedStorage.setInteger(getActivity(), APP_SETTINGS_PREFS, UHF_POWER, sbProgress);
                    }else{
                        if (((MainActivity) getActivity()).getUnfScanner().setPowerRFID(sbProgress)) {
                            SharedStorage.setInteger(getActivity(), APP_SETTINGS_PREFS, UHF_POWER, sbProgress);
                        } else {
                            ActivityUtils.showShortToast(getActivity(), getString(R.string.reader_power_application_error));
                            seekBar.setProgress(Integer.valueOf((SharedStorage.getInteger(getActivity(),
                                    APP_SETTINGS_PREFS, UHF_POWER, sbProgress) * 100) / savedPwrMax));
                        }
                    }
                }
            }
        });

        sbPowerRfid.setProgress(Integer.valueOf((savedPwr * 100)/savedPwrMax));
        final int finalSavedPwr = savedPwr;

        sbPowerRfid.post(() -> sbPowerRfid.setProgress(Integer.valueOf((finalSavedPwr * 100)/savedPwrMax)));

        List<OperatedLanguage> languages = Arrays.asList(OperatedLanguage.values());

        String lang = getResources().getConfiguration().locale.getLanguage();
        if (!TextUtils.isEmpty(lang)) {
            for (OperatedLanguage language: languages) {
                if(lang.equalsIgnoreCase(language.getCode())){
                    try {
                        etLanguage.setText(getString(languages.get(languages.indexOf(language)).getFullName()));
                    }catch (Exception e){}
                    break;
                }
            }
        }

        etLanguage.setOnClickListener(v -> {
            ActivityUtils.showSelectionListByAdapter(getActivity(), getString(R.string.select_language), null,
                    new LanguageAdapter(getActivity(), 0, 0, languages, true),
                    (item, value) -> {
                        String selectedLang = languages.get(item).getCode();
                        if(!TextUtils.isEmpty(selectedLang) &&
                                !lang.equalsIgnoreCase(selectedLang)){
                            SharedStorage.setString(getActivity(), APP_SETTINGS_PREFS, UI_LANG, selectedLang);
                            startActivity(new Intent(getActivity(), SplashActivity.class));
                            new Handler().postDelayed(() -> {getActivity().finish();}, 1000);
                        }
                    });
        });

//        if(DeviceUtils.getDeviceName().equals(Device.NAME_DEVICE_CHAINWAY)) {
//            initRfidStandard();
//        }else{
            etRfidStandardLayout.setVisibility(View.GONE);
//        }

        etTypeCon.setText(SharedStorage.getString(getActivity(), APP_SETTINGS_PREFS, TYPE_CONNECTION, TYPE_CONNECTION_HTTP));

        etTypeCon.setOnClickListener(view1 -> {
            List<String> selectionList = new ArrayList<>();
            selectionList.add(TYPE_CONNECTION_HTTP);
            selectionList.add(TYPE_CONNECTION_HTTPS);
            ActivityUtils.showSelectionList(
                    getActivity(),
                    getActivity().getString(R.string.type_connection),
                    null,
                    selectionList, (item, text) -> {
                        etTypeCon.setText(text.toString());
                    });
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_filter).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_geiger).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                SharedStorage.setString(getActivity(), APP_SETTINGS_PREFS, SERVER, etServer.getText().toString());
                SharedStorage.setString(getActivity(), APP_SETTINGS_PREFS, TYPE_CONNECTION, etTypeCon.getText().toString());
                SharedStorage.setInteger(getActivity(), APP_SETTINGS_PREFS, UHF_POWER, sbProgress);
                if(!fromLogin) {
                    ((MainActivity) getActivity()).getUnfScanner().setPowerRFID(sbProgress);
                }
                serviceApi.rebuildService();
                getActivity().onBackPressed();
                break;
            case R.id.rfid_standard:
//                showDialogSelectRfidStandard();
                return;
        }
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

//    private void initRfidStandard(){
//        rfidStandardList = getStandardList();
//
//        int idx = SharedStorage.getInteger(getActivity(), APP_SETTINGS_PREFS, RFID_STANDARD, 0x04);
//        if(!fromLogin) {
//            idx = ((MainActivity)getActivity()).getUnfScanner().getTagStandard();
//        }
//
//        int count = rfidStandardList.size();
//        initSelectionRfidStandard(idx > count - 1 ? count - 1 : idx);
//    }

//    private void initSelectionRfidStandard(int item){
//        if(item>rfidStandardList.size()-1 || item<0){
//            return;
//        }
//        SharedStorage.setInteger(getActivity(), APP_SETTINGS_PREFS, RFID_STANDARD, item);
//        etRfidStandard.setText(getModeName(item));
//
//    }
//
//    private List<String> getStandardList(){
//        List<String> selectionList = new ArrayList<>();
//        selectionList.add(getString(R.string.rfid_standard_1));
//        selectionList.add(getString(R.string.rfid_standard_2));
//        selectionList.add(getString(R.string.rfid_standard_4));
//        selectionList.add(getString(R.string.rfid_standard_8));
//        selectionList.add(getString(R.string.rfid_standard_16));
//        selectionList.add(getString(R.string.rfid_standard_32));
//        selectionList.add(getString(R.string.rfid_standard_33));
//        selectionList.add(getString(R.string.rfid_standard_34));
//        selectionList.add(getString(R.string.rfid_standard_80));
//
//        return selectionList;
//    }
//
//    private int getMode(String modeName) {
//        if (modeName.equals(getString(R.string.rfid_standard_1))) {
//            return 0x01;
//        } else if (modeName.equals(getString(R.string.rfid_standard_2))) {
//            return 0x02;
//        } else if (modeName.equals(getString(R.string.rfid_standard_4))) {
//            return 0x04;
//        } else if (modeName.equals(getString(R.string.rfid_standard_8))) {
//            return 0x08;
//        } else if (modeName.equals(getString(R.string.rfid_standard_16))) {
//            return 0x16;
//        } else if (modeName.equals(getString(R.string.rfid_standard_32))) {
//            return 0x32;
//        } else if (modeName.equals(getString(R.string.rfid_standard_33))) {
//            return 0x33;
//        } else if (modeName.equals(getString(R.string.rfid_standard_34))) {
//            return 0x34;
//        } else if (modeName.equals(getString(R.string.rfid_standard_80))) {
//            return 0x80;
//        }
//        return 0x04;
//    }
//    private String getModeName(int mode) {
//        switch (mode) {
//            case 0x01:
//                return getString(R.string.rfid_standard_1);
//            case 0x02:
//                return getString(R.string.rfid_standard_2);
//            case 0x04:
//                return getString(R.string.rfid_standard_4);
//            case 0x08:
//                return getString(R.string.rfid_standard_8);
//            case 0x16:
//                return getString(R.string.rfid_standard_16);
//            case 0x32:
//                return getString(R.string.rfid_standard_32);
//            case 0x33:
//                return getString(R.string.rfid_standard_33);
//            case 0x34:
//                return getString(R.string.rfid_standard_34);
//            case 0x80:
//                return getString(R.string.rfid_standard_80);
//            default:
//                return getString(R.string.rfid_standard_4);
//        }
//    }
//
//    private void showDialogSelectRfidStandard(){
//        if(rfidStandardList == null || rfidStandardList.size()==0){
//            return;
//        }
//
//        ActivityUtils.showSelectionList(
//                getActivity(),
//                getActivity().getString(R.string.rfid_standard),
//                null,
//                rfidStandardList, (item, text) -> {
//                    int mode = getMode(text.toString());
//                    if(!fromLogin) {
//                        if (((MainActivity) getActivity()).getUnfScanner().setTagStandard(mode)) {
//                            initSelectionRfidStandard(mode);
//                        } else {
//                            ActivityUtils.showMessage(getActivity(), null, null, getString(R.string.error_set_frind_standart));
//                        }
//                    }else{
//                        initSelectionRfidStandard(mode);
//                    }
//                });
//
//    }
}