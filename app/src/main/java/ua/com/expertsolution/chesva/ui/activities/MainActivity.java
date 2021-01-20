package ua.com.expertsolution.chesva.ui.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.app.InventoryApplication;
import ua.com.expertsolution.chesva.db.DataBase;
import ua.com.expertsolution.chesva.scanner.Device;
import ua.com.expertsolution.chesva.scanner.UnfScanner;
import ua.com.expertsolution.chesva.ui.fragments.FragmentInventoryListener;
import ua.com.expertsolution.chesva.ui.fragments.FragmentInventoryCallback;
import ua.com.expertsolution.chesva.ui.fragments.FragmentOnBackPressed;
import ua.com.expertsolution.chesva.ui.fragments.FragmentOnDispatchTouchEvent;
import ua.com.expertsolution.chesva.ui.fragments.MainFragment;
import ua.com.expertsolution.chesva.ui.fragments.searchmainasset.SearchMainAssetFragment;
import ua.com.expertsolution.chesva.utils.ActivityUtils;
import ua.com.expertsolution.chesva.utils.DeviceUtils;

import static ua.com.expertsolution.chesva.common.Consts.TAGLOG;

public class MainActivity extends BaseContextActivity {

    @Inject
    DataBase dataBase;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_container)
    LinearLayout toolbarContainer;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_search)
    ImageView toolbarSearch;

    private FragmentManager mFragmentManager;
    private Fragment selectedFragment;
    private Fragment fragmentRfidCallBack;
    private UnfScanner unfScanner;
    private ProgressDialog dialogLoad;

    private boolean doubleBackToExitPressedOnce = false;
    private boolean blockRfidScan = false;
    private AlertDialog alertDialogManyMessage;
    private String lastNotProcessedRfid;
    private long timeScanFirstLabel;
    private boolean startedOnKey = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ((InventoryApplication) getApplication()).getComponent().inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        mFragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            initSelectedFragment(MainFragment.newInstance());
        }

        initDialogLoad();

        unfScanner = new UnfScanner(this, new UnfScanner.ScannerGetTagCallback() {

            @Override
            public void onTagRead(String epc, String rssi) {
                if (fragmentRfidCallBack != null) {

                    if(fragmentRfidCallBack instanceof SearchMainAssetFragment) {
                        ((FragmentInventoryCallback) fragmentRfidCallBack).onTagRead(epc, rssi);
                        return;
                    }

                    if(alertDialogManyMessage!=null){
                        return;
                    }

                    if(!TextUtils.isEmpty(lastNotProcessedRfid) && !epc.equals(lastNotProcessedRfid)) {
                        if(alertDialogManyMessage==null) {
                            runOnUiThread(() -> {
                                showMessageToManyLabels();
                                stopRfidScan();
                                ((FragmentInventoryListener) fragmentRfidCallBack).stopInventory();
                            });
                        }
                        return;
                    }

                    if(!epc.equals(lastNotProcessedRfid)) {
                        lastNotProcessedRfid = epc;
                        timeScanFirstLabel = System.currentTimeMillis();
                        return;
                    }

                    if(timeScanFirstLabel > 0 && System.currentTimeMillis()-timeScanFirstLabel > 1000) {
                        ((FragmentInventoryCallback) fragmentRfidCallBack).onTagRead(epc, rssi);
                    }
                }
            }

            @Override
            public void onError(String text) {
                ActivityUtils.showShortToast(MainActivity.this, text);
            }
        });

        unfScanner.init(new UnfScanner.ScannerInitCallback() {
            @Override
            public void onStartInit() {
                showDialogLoad(getString(R.string.connecting_scanner),null);
            }

            @Override
            public void onFinishInit() {
                cancelDialogLoad();
            }

            @Override
            public void onErrorInit(String text) {
                cancelDialogLoad();
                ActivityUtils.showShortToast(MainActivity.this, text);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRfidScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unfScanner.destroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(startedOnKey){
            return super.onKeyDown(keyCode, event);
        }
        startedOnKey = true;
        switch (DeviceUtils.getDeviceName()){
            case Device.NAME_DEVICE_CHAINWAY:
                if (keyCode == 293 || keyCode == KeyEvent.KEYCODE_F9) {

                    if (fragmentRfidCallBack != null && !blockRfidScan) {
                        startRfidScan();
                        ((FragmentInventoryListener) fragmentRfidCallBack).startInventory();
                    }
                }
                break;
            case Device.NAME_DEVICE_ALIEN:
            case Device.NAME_DEVICE_CMC:
                if (keyCode == KeyEvent.KEYCODE_F7 || keyCode == KeyEvent.KEYCODE_F9) {

                    if (fragmentRfidCallBack != null && !blockRfidScan) {
                        startRfidScan();
                        ((FragmentInventoryListener) fragmentRfidCallBack).startInventory();
                    }
                }
                break;
//            case Device.NAME_DEVICE_NEWLAND:
//                if (keyCode == KeyEvent.KEYCODE_BUTTON_B) {
//                    if (fragmentRfidCallBack != null) {
//                        ((BarcodeListener) fragmentRfidCallBack).start();
//                    }
//                }
//                break;
//            case Device.NAME_DEVICE_SENTER_PAD_T:
//                if (keyCode == KeyEvent.KEYCODE_F1) {
//
//                    if (fragmentRfidCallBack != null) {
//                        startRfidScan();
//                        ((FragmentInventoryListener) fragmentRfidCallBack).startInventory();
//
//                    }
//                }
//                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        blockRfidScan = false;
        startedOnKey = false;
        switch (DeviceUtils.getDeviceName()){
            case Device.NAME_DEVICE_CHAINWAY:
                if (keyCode == 293 || keyCode == KeyEvent.KEYCODE_F9) {
                    if (fragmentRfidCallBack != null) {
                        stopRfidScan();
                        ((FragmentInventoryListener) fragmentRfidCallBack).stopInventory();
                    }
                }
                break;
            case Device.NAME_DEVICE_ALIEN:
            case Device.NAME_DEVICE_CMC:
                if (keyCode == KeyEvent.KEYCODE_F7 || keyCode == KeyEvent.KEYCODE_F9) {
                    if (fragmentRfidCallBack != null) {
                        stopRfidScan();
                        ((FragmentInventoryListener) fragmentRfidCallBack).stopInventory();
                    }
                }
                break;
//            case Device.NAME_DEVICE_NEWLAND:
//                break;
//            case Device.NAME_DEVICE_SENTER_PAD_T:
//                if (keyCode == KeyEvent.KEYCODE_F1) {
//                    if (fragmentRfidCallBack != null) {
//                        stopRfidScan();
//                        ((FragmentInventoryListener) fragmentRfidCallBack).stopInventory();
//                    }
//                }
//                break;
        }

        return super.onKeyUp(keyCode, event);
    }

    public Boolean setMaskScan(String maskScan) {
        return unfScanner.setMaskScan(maskScan);
    }

    public void setBlockRfidScan() {
        this.blockRfidScan = true;
    }

    public void startRfidScan(){
        lastNotProcessedRfid = null;
        timeScanFirstLabel = 0;
        unfScanner.startRfidScan();
    }

    public void stopRfidScan(){
        unfScanner.stopRfidScan();
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public ImageView getToolbarSearch() {
        return toolbarSearch;
    }

    public void setTextTitle(String text){
        toolbarTitle.setText(text);
    }

    public void changeToolbar(String title, boolean enable, boolean showHomwButton, boolean isClose) {
        toolbarTitle.setText(title);
        if(enable) {
            toolbarSearch.setVisibility(View.GONE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            toolbarSearch.setVisibility(View.GONE);
            if(showHomwButton) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                if (isClose) {
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white);
                } else {
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
                }
            }else{
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    public void setFragmentRfidCallBack(Fragment fragmentRfidCallBack) {
        this.fragmentRfidCallBack = fragmentRfidCallBack;
    }

    public UnfScanner getUnfScanner() {
        return unfScanner;
    }

    @Override
    public void onBackPressed() {
        if(selectedFragment instanceof FragmentOnBackPressed &&
                 !((FragmentOnBackPressed) selectedFragment).onBackPressed()) {
            int index = mFragmentManager.getBackStackEntryCount();
            if (index == 0) {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                }

                doubleBackToExitPressedOnce = true;
                Toast.makeText(MainActivity.this,
                        getString(R.string.double_press_exit), Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(() -> {
                    doubleBackToExitPressedOnce = false;
                }, 2000);
            } else {
                popBackStack();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(selectedFragment instanceof FragmentOnDispatchTouchEvent){
            ((FragmentOnDispatchTouchEvent) selectedFragment).dispatchTouchEvent(event);
        }
        return super.dispatchTouchEvent( event );
    }

    public void popBackStack() {
        try {
            mFragmentManager.popBackStack();
        }catch (Exception e){
            Log.e(TAGLOG, e.toString());
        }
    }

    private void initDialogLoad() {

        dialogLoad = new ProgressDialog(this);
        dialogLoad.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialogLoad.setMessage(getString(R.string.loading_title));
        dialogLoad.setIndeterminate(true);
        dialogLoad.setCanceledOnTouchOutside(false);

    }

    public void showDialogLoad(String title, String message) {
        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(message)){
            dialogLoad.setTitle(title);
            dialogLoad.setMessage(message);
        }else if(!TextUtils.isEmpty(title)){
            dialogLoad.setTitle(title);
            dialogLoad.setMessage("");
        }else {
            dialogLoad.setTitle(getString(R.string.loading_title));
            dialogLoad.setMessage("");
        }
        try {
            dialogLoad.show();
        } catch(Exception e){
            Log.e(TAGLOG, e.toString());
        }
    }

    public void cancelDialogLoad() {
        if(dialogLoad!=null) {
            try {
                dialogLoad.cancel();
            }catch (Exception e){ }
        }
    }

    private void initSelectedFragment(Fragment newFragment){

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        selectedFragment = newFragment;
        ft.replace(R.id.main_container, selectedFragment).commitAllowingStateLoss();

    }

    public void showNextFragment(Fragment newFragment){

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        selectedFragment = newFragment;
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        ft.replace(R.id.main_container, selectedFragment);
        ft.addToBackStack(null);
        ft.commit();

    }

    public void showUpFragment(Fragment newFragment){

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        selectedFragment = newFragment;
        ft.setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_top);
        ft.replace(R.id.main_container, selectedFragment);
        ft.addToBackStack(null);
        ft.commit();

    }

    public void showDownFragment(Fragment newFragment){

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        selectedFragment = newFragment;
        ft.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom);
        ft.replace(R.id.main_container, selectedFragment);
        ft.addToBackStack(null);
        ft.commit();

    }

    public void showMessageToManyLabels() {
        if(alertDialogManyMessage!=null){
            return;
        }
        alertDialogManyMessage = ActivityUtils.showMessageWihtCallBack(MainActivity.this,
                getString(R.string.attention), null, getString(R.string.to_many_labels),
                getString(R.string.questions_answer_ok), (ActivityUtils.MessageCallBack) () -> {
                    alertDialogManyMessage = null;
                    lastNotProcessedRfid = null;
                    if (fragmentRfidCallBack != null) {
                        stopRfidScan();
                        ((FragmentInventoryListener) fragmentRfidCallBack).stopInventory();
                    }
                });
    }
}