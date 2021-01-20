package ua.com.expertsolution.chesva.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.expertsolution.chesva.BuildConfig;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.ui.fragments.FragmentOnBackPressed;
import ua.com.expertsolution.chesva.ui.fragments.login.LoginFragment;
import ua.com.expertsolution.chesva.utils.AESEncyption;
import ua.com.expertsolution.chesva.utils.ActivityUtils;
import ua.com.expertsolution.chesva.utils.PermissionUtils;
import ua.com.expertsolution.chesva.utils.SharedStorage;

import static ua.com.expertsolution.chesva.common.Consts.APP_SETTINGS_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.PERIOD_LICENSE;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG;

public class SplashActivity extends BaseContextActivity {
    private static final int REQUEST_PERMISSIONS = 555;
    private static final int UPLOAD_FILE_REQUEST_CODE = 556;
    private static final int READ_FILE_REQUEST_CODE = 557;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private FragmentManager mFragmentManager;
    private Fragment selectedFragment;
    private boolean doubleBackToExitPressedOnce = false;
    private ProgressDialog dialogLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!PermissionUtils.checkPermissions(this, REQUEST_PERMISSIONS)){
            startActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_PERMISSIONS){
            startActivity();
        }
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
                Toast.makeText(SplashActivity.this,
                        getString(R.string.double_press_exit), Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(() -> {
                    doubleBackToExitPressedOnce = false;
                }, 2000);
            } else {
                popBackStack();
            }
        }
    }

    public void popBackStack() {
        try {
            mFragmentManager.popBackStack();
        }catch (Exception e){
            Log.e(TAGLOG, e.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UPLOAD_FILE_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    Log.i(TAGLOG, "Uri: " + uri.toString());

                    try {
                        String pathFile = uri.getPath().split(":")[1];
                        pathFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + pathFile;
                        FileWriter writer = new FileWriter(new File(pathFile));
                        String text = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                        writer.append(AESEncyption.encrypt(text));
                        writer.flush();
                        writer.close();
                        showActivationMessage();
                        ActivityUtils.showShortToast(this, getString(R.string.file_license_create));
                    } catch (Exception e) {
                        e.printStackTrace();
                        showActivationMessage();
                        ActivityUtils.showShortToast(this, getString(R.string.error_file_license_create));
                    }
                } else {
                    showActivationMessage();
                }
                break;
            case READ_FILE_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    Log.i(TAGLOG, "Uri: " + uri.toString());
                    String pathFile = uri.getPath().split(":")[1];
                    pathFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + pathFile;
                    StringBuilder text = new StringBuilder();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(new File(pathFile)));
                        String line;

                        while ((line = br.readLine()) != null) {
                            text.append(line);
                        }
                        br.close();

                        byte[] dataFile = Base64.decode(text.toString(), Base64.DEFAULT);
                        Date LicenseDate = new SimpleDateFormat("yyyy-MM-dd hh:mm")
                                .parse(new String(dataFile, "UTF-8").substring(0, 16));
                        String code = AESEncyption.decrypt(new String(dataFile, "UTF-8").substring(16));

                        if (Settings.Secure
                                .getString(getContentResolver(), Settings.Secure.ANDROID_ID)
                                .equalsIgnoreCase(code)) {

                            if (new Date().getTime() < LicenseDate.getTime()) {
                                SharedStorage.setLong(this, APP_SETTINGS_PREFS, PERIOD_LICENSE, LicenseDate.getTime());
                                ActivityUtils.showShortToast(this, getString(R.string.license_activated));
                                startActivity();
                            } else {
                                showActivationMessage();
                                ActivityUtils.showShortToast(this, getString(R.string.license_period_end));
                            }
                        } else {
                            showActivationMessage();
                            ActivityUtils.showShortToast(this, getString(R.string.license_device_error));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showActivationMessage();
                        ActivityUtils.showShortToast(this, getString(R.string.error_read_file));
                    }
                } else {
                    showActivationMessage();
                }
                break;
        }
    }

    private void startActivity(){

        if (new Date().getTime() > SharedStorage.getLong(this, APP_SETTINGS_PREFS, PERIOD_LICENSE, 0)
                && !BuildConfig.DEBUG) {
            showLicenseMessage();
            return;
        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        mFragmentManager = getSupportFragmentManager();

        initSelectedFragment(LoginFragment.newInstance());

        initDialogLoad();
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
            dialogLoad.setTitle("");
            dialogLoad.setMessage(getString(R.string.loading_title));
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

    public void changeToolbar(boolean enable, String title) {
        if(enable) {
            setTheme(R.style.AppTheme);
            getSupportActionBar().show();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ContextCompat.getColor(SplashActivity.this, android.R.color.white));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary, this.getTheme()));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        } else {
            setTheme(R.style.SplashTheme);
            getSupportActionBar().hide();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ContextCompat.getColor(SplashActivity.this,R.color.colorPrimaryDark));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorBackground, this.getTheme()));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorBackground));
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                int statusBarHeight = ActivityUtils.getStatusBarHeight(this);
                View view = new View(this);
                view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                view.getLayoutParams().height = statusBarHeight;
                ((ViewGroup) getWindow().getDecorView()).addView(view);
                view.setBackgroundColor(getResources().getColor(R.color.colorBackground));
            }
        }
    }

    private void initSelectedFragment(Fragment newFragment){

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        selectedFragment = newFragment;
        ft.replace(R.id.main_container, newFragment).commitAllowingStateLoss();

    }

    public void showUpFragment(Fragment newFragment){

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        selectedFragment = newFragment;
        ft.setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_top);
        ft.replace(R.id.main_container, selectedFragment);
        ft.addToBackStack(null);
        ft.commit();

    }

    private void showLicenseMessage() {

        ActivityUtils.showQuestion(this, getString(R.string.license), null,
                getString(R.string.license_period_end),
                getString(R.string.questions_answer_exit),
                getString(R.string.questions_answer_license_activate), null, new ActivityUtils.QuestionAnswer() {
                    @Override
                    public void onPositiveAnswer() {
                        finish();
                    }

                    @Override
                    public void onNegativeAnswer() {
                        showActivationMessage();
                    }

                    @Override
                    public void onNeutralAnswer() {
                    }
                });

    }

    private void showActivationMessage() {

        ActivityUtils.showQuestion(this, getString(R.string.questions_answer_license_activate), null,
                getString(R.string.license_activate_operation),
                getString(R.string.questions_answer_exit),
                getString(R.string.questions_answer_license_upload_file),
                getString(R.string.questions_answer_license_load_file), new ActivityUtils.QuestionAnswer() {
                    @Override
                    public void onPositiveAnswer() {
                        finish();
                    }

                    @Override
                    public void onNegativeAnswer() {
                        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("text/lic");//("*/*");
                        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.license_file_name));
                        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.fromFile(android.os.Environment.getExternalStorageDirectory()));
                        startActivityForResult(intent, UPLOAD_FILE_REQUEST_CODE);
                    }

                    @Override
                    public void onNeutralAnswer() {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.fromFile(android.os.Environment.getExternalStorageDirectory()));
                        startActivityForResult(intent, READ_FILE_REQUEST_CODE);
                    }
                });

    }

}
