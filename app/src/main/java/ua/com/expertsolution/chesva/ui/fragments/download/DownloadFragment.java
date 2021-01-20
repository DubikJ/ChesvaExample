package ua.com.expertsolution.chesva.ui.fragments.download;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.model.LoadEventHandler;
import ua.com.expertsolution.chesva.ui.activities.MainActivity;
import ua.com.expertsolution.chesva.ui.fragments.FragmentOnBackPressed;
import ua.com.expertsolution.chesva.utils.ActivityUtils;
import ua.com.expertsolution.chesva.utils.SharedStorage;

import static ua.com.expertsolution.chesva.common.Consts.APP_SETTINGS_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.IS_DB_NO_EMPTY;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_DOWNLOAD;

public class DownloadFragment extends Fragment implements View.OnClickListener, FragmentOnBackPressed {
    private static final String FROM_FILE = "from_file";
    private static final int READ_REQUEST_CODE = 400;

    @BindView(R.id.progress_bar_load)
    ProgressBar pbLoad;

    @BindView(R.id.tv_state)
    TextView tvState;

    @BindView(R.id.tv_load_state)
    TextView tvLoadState;

    @BindView(R.id.btnLoad)
    Button btnLoad;

    @BindView(R.id.btnLoadCancel)
    Button btnLoadCancel;

    private View view;
    private DownloadViewModel mViewModel;
    private AsyncTask asyncLoad;
    private boolean clearPreviousData = false;
    private boolean fromFile = false;

    public static DownloadFragment getInstance(boolean fromFile) {

        Bundle args = new Bundle();
        args.putBoolean(FROM_FILE, fromFile);
        DownloadFragment fragment = new DownloadFragment();
        fragment.setArguments(args);
        return  fragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            fromFile = bundle.getBoolean(FROM_FILE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);
        ButterKnife.bind(this, view);
        readBundle(getArguments());

        mViewModel = new ViewModelProvider(this).get(DownloadViewModel.class);
        mViewModel.setActivityContext(getActivity());

        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).changeToolbar(getActivity().getString(R.string.loading),
                false, true, false);
        ((MainActivity)getActivity()).setFragmentRfidCallBack(null);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel.getResultLoad().observe(getViewLifecycleOwner(), newResult -> {
            switch (newResult.getStatus()) {
                case LoadEventHandler.LOAD_STARTED:
                    btnLoad.setEnabled(false);
                    btnLoadCancel.setEnabled(true);
                    pbLoad.setVisibility(View.VISIBLE);
                    pbLoad.setIndeterminate(true);
                    tvState.setVisibility(View.VISIBLE);
                    tvState.setText(getString(R.string.downloading));
                    pbLoad.setProgress(0);
                    break;
                case LoadEventHandler.LOAD_PROGRESS:
                    pbLoad.setVisibility(View.VISIBLE);
                    pbLoad.setIndeterminate(false);
                    pbLoad.setProgress(newResult.getMaxProgress() == 0 ? 0 :
                            (int)((newResult.getProgress()*100)/newResult.getMaxProgress()));
                    tvLoadState.setVisibility(View.VISIBLE);
                    tvLoadState.setText(newResult.getProgress() + " " +
                            getString(R.string.from) + " " + newResult.getMaxProgress());
                    tvState.setText(getString(R.string.downloading) +
                            (TextUtils.isEmpty(newResult.getText()) ? "" : ": " + newResult.getText()));
                    break;
                case LoadEventHandler.LOAD_DISMISS:
                    pbLoad.setVisibility(View.INVISIBLE);
                    tvLoadState.setVisibility(View.INVISIBLE);
                    tvState.setText(getString(R.string.canceled));
                    btnLoad.setEnabled(true);
                    btnLoadCancel.setEnabled(false);
                    break;
                case LoadEventHandler.LOAD_ERROR:
                    pbLoad.setVisibility(View.INVISIBLE);
                    tvLoadState.setVisibility(View.INVISIBLE);
                    tvState.setText(getString(R.string.error_download) +
                            (TextUtils.isEmpty(newResult.getText()) ? "" : ": " + newResult.getText()));
                    btnLoad.setEnabled(true);
                    btnLoadCancel.setEnabled(false);
                    break;
                case LoadEventHandler.LOAD_OPEN_FILE:
                    pbLoad.setVisibility(View.VISIBLE);
                    pbLoad.setIndeterminate(true);
                    tvState.setText(getString(R.string.opening_file));
                    break;
                case LoadEventHandler.LOAD_CLEAR_PREVIOUS_DATA:
                    pbLoad.setVisibility(View.VISIBLE);
                    pbLoad.setIndeterminate(true);
                    tvState.setText(getString(R.string.clearing_previous_data));
                    break;
                case LoadEventHandler.LOAD_SAVING_TO_DB:
                    pbLoad.setVisibility(View.VISIBLE);
                    pbLoad.setIndeterminate(true);
                    tvState.setText(getString(R.string.saving_to_db));
                    break;
                case LoadEventHandler.LOAD_FINISH:
                    pbLoad.setVisibility(View.INVISIBLE);
                    tvLoadState.setVisibility(View.INVISIBLE);
                    tvState.setText(getString(R.string.load_done));
                    btnLoad.setEnabled(true);
                    btnLoadCancel.setEnabled(false);
                    break;
            }
        });

        btnLoad.setOnClickListener(this);
        btnLoadCancel.setOnClickListener(this);
        btnLoadCancel.setEnabled(false);
//
//        pbDownloadState.getProgressDrawable().setColorFilter(
//                getResources().getColor(R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
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
            case R.id.action_settings:
                return true;
            case R.id.home:
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void performOpenFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.fromFile(android.os.Environment.getExternalStorageDirectory()));
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case READ_REQUEST_CODE:
                    if (resultData != null) {
                        Uri uri = resultData.getData();
                        Log.i(TAGLOG_DOWNLOAD, "Uri: " + uri.toString());
                        asyncLoad = mViewModel.loadFromFile(uri, clearPreviousData);
                    }
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnLoad:
                if(SharedStorage.getBoolean(getActivity(), APP_SETTINGS_PREFS, IS_DB_NO_EMPTY, false)) {
                    clearPreviousData = false;
                    ActivityUtils.showQuestion(getActivity(), null, null,
                            getString(R.string.question_previous_data_will_be_clean),
                            getString(R.string.questions_answer_continue),
                            getString(R.string.questions_answer_cancel),
                            null, new ActivityUtils.QuestionAnswer() {
                                @Override
                                public void onPositiveAnswer() {
                                    clearPreviousData = true;
                                    if(fromFile){
                                        performOpenFile();
                                    }else {
                                        mViewModel.loadFromServer(true);
                                    }
                                }

                                @Override
                                public void onNegativeAnswer() { }

                                @Override
                                public void onNeutralAnswer() {
                                }
                            });
                }else{
                    if(fromFile){
                        performOpenFile();
                    }else {
                        mViewModel.loadFromServer(clearPreviousData);
                    }
                }

                break;
            case R.id.btnLoadCancel:
                if(asyncLoad!=null){
                    asyncLoad.cancel(false);
                    asyncLoad = null;
                    tvLoadState.setText(getString(R.string.start_cancel));
                    btnLoadCancel.setEnabled(false);
                }
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if(asyncLoad!=null){
            asyncLoad.cancel(false);
            asyncLoad = null;
        }
        return false;
    }
}