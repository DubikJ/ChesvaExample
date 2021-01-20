package ua.com.expertsolution.chesva.ui.activities;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import ua.com.expertsolution.chesva.utils.AppUtils;

public class BaseContextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.setLocale(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        AppUtils.setLocale(this);
        super.onConfigurationChanged(newConfig);
    }

}
