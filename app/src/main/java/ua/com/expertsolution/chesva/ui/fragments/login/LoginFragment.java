package ua.com.expertsolution.chesva.ui.fragments.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.adapter.LanguageAdapter;
import ua.com.expertsolution.chesva.model.LoadEventHandler;
import ua.com.expertsolution.chesva.model.OperatedLanguage;
import ua.com.expertsolution.chesva.model.json.AuthResponse;
import ua.com.expertsolution.chesva.ui.activities.MainActivity;
import ua.com.expertsolution.chesva.ui.activities.SplashActivity;
import ua.com.expertsolution.chesva.ui.fragments.FragmentOnBackPressed;
import ua.com.expertsolution.chesva.ui.fragments.settings.SettingsFragment;
import ua.com.expertsolution.chesva.utils.ActivityUtils;
import ua.com.expertsolution.chesva.utils.SharedStorage;

import static ua.com.expertsolution.chesva.common.Consts.APP_SETTINGS_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.ENTERED_LOGIN;
import static ua.com.expertsolution.chesva.common.Consts.ENTERED_PASSWORD;
import static ua.com.expertsolution.chesva.common.Consts.UI_LANG;

public class LoginFragment extends Fragment implements View.OnClickListener, FragmentOnBackPressed {
    private static final int READ_REQUEST_CODE = 400;

    @BindView(R.id.sLanguage)
    Spinner sLanguage;

    @BindView(R.id.settings)
    FrameLayout settings;

    @BindView(R.id.login_layout)
    TextInputLayout loginLayout;

    @BindView(R.id.login)
    EditText loginET;

    @BindView(R.id.password_layout)
    TextInputLayout passwordLayout;

    @BindView(R.id.password)
    EditText passwordET;

    @BindView(R.id.cb_save_pass)
    CheckBox savePassCB;

    @BindView(R.id.btn_login)
    Button loginBTN;

    private View view;
    private LoginViewModel mViewModel;
    private Call<AuthResponse> responseCall;
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ((SplashActivity) getActivity()).changeToolbar(false, null);

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);

        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        mViewModel.setActivityContext(getActivity());

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loginET.setText(SharedStorage.getString(getActivity(),APP_SETTINGS_PREFS, ENTERED_LOGIN, ""));
        String p = SharedStorage.getString(getActivity(),APP_SETTINGS_PREFS, ENTERED_PASSWORD, "");
        if(!TextUtils.isEmpty(p)) {
            passwordET.setText(p);
            savePassCB.setChecked(true);
        }

        mViewModel.getResultLoad().observe(getViewLifecycleOwner(), newResult -> {
            switch (newResult.getStatus()) {
                case LoadEventHandler.LOAD_STARTED:
                    loginBTN.setEnabled(false);
                    ((SplashActivity)getActivity()).showDialogLoad(null, null);
                    break;
                case LoadEventHandler.LOAD_DISMISS:
                    loginBTN.setEnabled(true);
                    ((SplashActivity)getActivity()).cancelDialogLoad();
                    break;
                case LoadEventHandler.LOAD_ERROR:
                    passwordLayout.setError((TextUtils.isEmpty(newResult.getText())
                            ? getString(R.string.error_sync) : newResult.getText()));
                    loginBTN.setEnabled(true);
                    ((SplashActivity)getActivity()).cancelDialogLoad();
                    break;
                case LoadEventHandler.LOAD_FINISH:
                    ((SplashActivity)getActivity()).cancelDialogLoad();
                    loginBTN.setEnabled(false);
                    getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
                    new Handler().postDelayed(() -> {
                        getActivity().finish();
                    }, 2000);
                    break;
            }
        });

        settings.setOnClickListener(this);
        loginBTN.setOnClickListener(this);

        List<OperatedLanguage> languages = Arrays.asList(OperatedLanguage.values());

        sLanguage.setAdapter(new LanguageAdapter(getActivity(), 0, 0, languages, false));
        String lang = getResources().getConfiguration().locale.getLanguage();
        if (!TextUtils.isEmpty(lang)) {
            for (OperatedLanguage language: languages) {
                if(lang.equalsIgnoreCase(language.getCode())){
                    sLanguage.setSelection(languages.indexOf(language));
                    break;
                }
            }
        }

        sLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String selectedLang = languages.get(position).getCode();
                if(!TextUtils.isEmpty(selectedLang) &&
                        !lang.equalsIgnoreCase(selectedLang)){
                    SharedStorage.setString(getActivity(), APP_SETTINGS_PREFS, UI_LANG, selectedLang);
                    getActivity().recreate();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        loginET.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                passwordET.requestFocus();
            }
            return handled;
        });

        passwordET.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginBTN.requestFocus();
            }
            return handled;
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
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_login:
                loginLayout.setError("");
                passwordLayout.setError("");
                if(TextUtils.isEmpty(loginET.getText().toString())){
                    loginLayout.setError(getString(R.string.error_field_required));
                    return;
                }
                if(TextUtils.isEmpty(passwordET.getText().toString())){
                    passwordLayout.setError(getString(R.string.error_field_required));
                    return;
                }
                SharedStorage.setString(getActivity(),APP_SETTINGS_PREFS, ENTERED_LOGIN, loginET.getText().toString());
                if(savePassCB.isChecked()) {
                    SharedStorage.setString(getActivity(), APP_SETTINGS_PREFS, ENTERED_PASSWORD, passwordET.getText().toString());
                }else{
                    SharedStorage.setString(getActivity(), APP_SETTINGS_PREFS, ENTERED_PASSWORD, "");
                }
                mViewModel.authenticate("password", loginET.getText().toString(), passwordET.getText().toString());
                break;
            case R.id.settings:
                ActivityUtils.showDialogPassword(getActivity(), () ->
                        ((SplashActivity)getActivity()).showUpFragment(SettingsFragment.getInstance(true)));
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if(responseCall!=null){
            responseCall.cancel();
            responseCall = null;
        }
        return false;
    }
}