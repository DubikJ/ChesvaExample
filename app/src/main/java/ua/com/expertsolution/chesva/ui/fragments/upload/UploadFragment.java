package ua.com.expertsolution.chesva.ui.fragments.upload;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.ui.activities.MainActivity;
import ua.com.expertsolution.chesva.ui.fragments.FragmentOnBackPressed;
import ua.com.expertsolution.chesva.utils.ActivityUtils;

import static ua.com.expertsolution.chesva.common.Consts.DATE_DAY_FORMAT;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_DOWNLOAD;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_UPLOAD;
import static ua.com.expertsolution.chesva.model.LoadEventHandler.UPLOAD_DISMISS;
import static ua.com.expertsolution.chesva.model.LoadEventHandler.UPLOAD_ERROR;
import static ua.com.expertsolution.chesva.model.LoadEventHandler.UPLOAD_FINISH;
import static ua.com.expertsolution.chesva.model.LoadEventHandler.UPLOAD_PROGRESS;
import static ua.com.expertsolution.chesva.model.LoadEventHandler.UPLOAD_SAVING_TO_FILE;
import static ua.com.expertsolution.chesva.model.LoadEventHandler.UPLOAD_STARTED;

public class UploadFragment extends Fragment implements View.OnClickListener, FragmentOnBackPressed {
    private static final String FROM_FILE = "from_file";
    private static final int WRITE_REQUEST_CODE = 401;

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

    @BindView(R.id.shareImage)
    ImageView shareImage;

    private View view;
    private UploadViewModel mViewModel;
    private AsyncTask asyncLoad;
    private String pathFile;
    private boolean fromFile = false;

    public static UploadFragment getInstance(boolean fromFile) {

        Bundle args = new Bundle();
        args.putBoolean(FROM_FILE, fromFile);
        UploadFragment fragment = new UploadFragment();
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
        View view = inflater.inflate(R.layout.fragment_upload, container, false);
        ButterKnife.bind(this, view);
        readBundle(getArguments());

        mViewModel = new ViewModelProvider(this).get(UploadViewModel.class);
        mViewModel.setActivityContext(getActivity());

        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).changeToolbar(getActivity().getString(R.string.up_loading),
                false, true, false);
        ((MainActivity)getActivity()).setFragmentRfidCallBack(null);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.getResultUpLoad().observe(getViewLifecycleOwner(), newResult -> {
            switch (newResult.getStatus()) {
                case UPLOAD_STARTED:
                    btnLoad.setEnabled(false);
                    btnLoadCancel.setEnabled(true);
                    pbLoad.setVisibility(View.VISIBLE);
                    pbLoad.setIndeterminate(true);
                    tvState.setVisibility(View.VISIBLE);
                    pbLoad.setProgress(0);
                    if(fromFile) {
                        tvState.setText(getString(R.string.get_data_from_db));
                    }else {
                        tvState.setText(getString(R.string.uploading));
                    }
                    shareImage.setVisibility(View.GONE);
                    pathFile = null;
                    break;
                case UPLOAD_PROGRESS:
                    pbLoad.setVisibility(View.VISIBLE);
                    pbLoad.setIndeterminate(false);
                    pbLoad.setProgress(newResult.getMaxProgress() == 0 ?
                            0 : (int)((newResult.getProgress()*100)/newResult.getMaxProgress()));
                    tvLoadState.setVisibility(View.VISIBLE);
                    tvLoadState.setText(newResult.getProgress() + " " +
                            getString(R.string.from) + " " + newResult.getMaxProgress());
                    if(fromFile){
                        tvState.setText(getString(R.string.write_to_table) +
                                (TextUtils.isEmpty(newResult.getText()) ? "" : ": " + newResult.getText()));
                    }else {
                        tvState.setText(getString(R.string.uploading) +
                                (TextUtils.isEmpty(newResult.getText()) ? "" : ": " + newResult.getText()));
                    }
                    break;
                case UPLOAD_DISMISS:
                    pbLoad.setVisibility(View.INVISIBLE);
                    tvLoadState.setVisibility(View.INVISIBLE);
                    tvState.setText(getString(R.string.canceled));
                    btnLoad.setEnabled(true);
                    btnLoadCancel.setEnabled(false);
                    break;
                case UPLOAD_ERROR:
                    pbLoad.setVisibility(View.INVISIBLE);
                    tvLoadState.setVisibility(View.INVISIBLE);
                    tvState.setText(getString(R.string.error_upload) +
                            (TextUtils.isEmpty(newResult.getText()) ? "" : ": " + newResult.getText()));
                    btnLoad.setEnabled(true);
                    btnLoadCancel.setEnabled(false);
                    break;
                case UPLOAD_SAVING_TO_FILE:
                    tvState.setText(getString(R.string.writing_file));
                    pbLoad.setVisibility(View.VISIBLE);
                    pbLoad.setIndeterminate(true);
                    break;
                case UPLOAD_FINISH:
                    pbLoad.setVisibility(View.INVISIBLE);
                    tvLoadState.setVisibility(View.INVISIBLE);
                    tvState.setText(getString(R.string.load_done));
                    btnLoad.setEnabled(true);
                    btnLoadCancel.setEnabled(false);
                    if(fromFile) {
                        shareImage.setVisibility(View.VISIBLE);
                    }
                    pathFile = null;
                    break;
            }
        });

        btnLoad.setOnClickListener(this);
        btnLoadCancel.setOnClickListener(this);
        btnLoadCancel.setEnabled(false);
        shareImage.setOnClickListener(this);

//        pbUploadState.getProgressDrawable().setColorFilter(
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

    public void performSaveFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
//        intent.setType("application/vnd.ms-excel");
        intent.putExtra(Intent.EXTRA_TITLE, DATE_DAY_FORMAT.format(new Date()));
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.fromFile(android.os.Environment.getExternalStorageDirectory()));
        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case WRITE_REQUEST_CODE:
                    if (resultData != null) {
                        Uri uri = resultData.getData();
                        if(!uri.toString().contains("externalstorage")){
                            ActivityUtils.showMessage(getActivity(), null, null, getString(R.string.select_storage_directory));
                            return;
                        }
                        Log.i(TAGLOG_DOWNLOAD, "Uri: " + uri.toString());
                        asyncLoad = mViewModel.writeToFile(uri);
                    }
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnLoad:
                if(fromFile){
                    performSaveFile();
                }else {
                    mViewModel.uploadToServer();
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
            case R.id.shareImage:
                if(TextUtils.isEmpty(pathFile)){
                    share(pathFile);
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

    @SuppressLint("LongLogTag")
    private void share(String fileName) {
        Uri fileUri = Uri.parse("content://" + getActivity().getPackageName() + "/" + fileName);
        Log.d(TAGLOG_UPLOAD, "sending "+fileUri.toString()+" ...");
        Intent shareIntent = new Intent();
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setType("application/octet-stream");
        startActivity(Intent.createChooser(shareIntent, "Send to..."));
    }
}