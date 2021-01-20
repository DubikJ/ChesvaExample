package ua.com.expertsolution.chesva.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.ui.activities.MainActivity;
import ua.com.expertsolution.chesva.ui.fragments.download.DownloadFragment;
import ua.com.expertsolution.chesva.ui.fragments.upload.UploadFragment;


public class SelectLoadFragment extends Fragment implements View.OnClickListener, FragmentOnBackPressed {
    private static final String IS_DOWNLOAD = "is_download";

    @BindView(R.id.btnLoadWifi)
    Button btnLoadWifi;

    @BindView(R.id.btnLoadFile)
    Button btnLoadFile;

    private View view;
    private ImageView toolbarSearch;
    private Menu menu;
    private boolean isDownload = false;

    public static SelectLoadFragment getInstance(boolean isDownload) {

        Bundle args = new Bundle();
        args.putBoolean(IS_DOWNLOAD, isDownload);
        SelectLoadFragment fragment = new SelectLoadFragment();
        fragment.setArguments(args);
        return  fragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            isDownload = bundle.getBoolean(IS_DOWNLOAD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_load, container, false);
        ButterKnife.bind(this, view);
        readBundle(getArguments());

        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).changeToolbar(isDownload ? getActivity().getString(R.string.loading)
                : getActivity().getString(R.string.up_loading), false, true, false);
        ((MainActivity)getActivity()).setFragmentRfidCallBack(null);

        toolbarSearch = ((MainActivity)getActivity()).getToolbarSearch();

        btnLoadWifi.setOnClickListener(this);
        btnLoadFile.setOnClickListener(this);
        if(isDownload){
            btnLoadWifi.setText(getString(R.string.download_wifi));
            btnLoadFile.setText(getString(R.string.download_file));
        }else{
            btnLoadWifi.setText(getString(R.string.upload_wifi));
            btnLoadFile.setText(getString(R.string.upload_file));
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_filter).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_geiger).setVisible(false);
        this.menu = menu;
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
        switch(view.getId()) {
            case R.id.btnLoadWifi:
                if(isDownload) {
                    ((MainActivity) getActivity()).showNextFragment(DownloadFragment.getInstance(false));
                }else{
                    ((MainActivity) getActivity()).showNextFragment(UploadFragment.getInstance(false));
                }
                break;
            case R.id.btnLoadFile:
                if(isDownload) {
                    ((MainActivity) getActivity()).showNextFragment(DownloadFragment.getInstance(true));
                }else{
                    ((MainActivity) getActivity()).showNextFragment(UploadFragment.getInstance(true));
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
