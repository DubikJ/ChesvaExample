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
import ua.com.expertsolution.chesva.ui.fragments.geiger.GeigerFragment;
import ua.com.expertsolution.chesva.ui.fragments.settings.SettingsFragment;
import ua.com.expertsolution.chesva.utils.ActivityUtils;
import ua.com.expertsolution.chesva.utils.SharedStorage;
import ua.com.expertsolution.chesva.common.Consts;


public class MainFragment extends Fragment implements View.OnClickListener, FragmentOnBackPressed {

    @BindView(R.id.btnMainDownloadFile)
    Button btnMainDownloadFile;

    @BindView(R.id.btnMainUploadFile)
    Button btnMainUploadFile;

    @BindView(R.id.btnMainInventory)
    Button btnMainInventory;

    private View view;
    private ImageView toolbarSearch;
    private Menu menu;

    public static MainFragment newInstance() {

        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return  fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).changeToolbar(getActivity().getString(R.string.main_menu), true, false, false);
        ((MainActivity)getActivity()).setFragmentRfidCallBack(null);

        toolbarSearch = ((MainActivity)getActivity()).getToolbarSearch();

        btnMainInventory.setOnClickListener(this);
        btnMainDownloadFile.setOnClickListener(this);
        btnMainUploadFile.setOnClickListener(this);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_filter).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(true);
        menu.findItem(R.id.action_geiger).setVisible(false);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_geiger:
                ((MainActivity)getActivity()).showUpFragment(GeigerFragment.newInstance());
                return true;
            case R.id.action_settings:
                ActivityUtils.showDialogPassword(getActivity(), () ->
                        ((MainActivity)getActivity()).showUpFragment(SettingsFragment.getInstance(false)));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnMainDownloadFile:
                ((MainActivity)getActivity()).showNextFragment(SelectLoadFragment.getInstance(true));
                break;
            case R.id.btnMainUploadFile:
                ((MainActivity)getActivity()).showNextFragment(SelectLoadFragment.getInstance(false));
                break;
            case R.id.btnMainInventory:
                ((MainActivity)getActivity()).showNextFragment(SelectOperationFragment.getInstance());
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SharedStorage.getBoolean(getActivity(), Consts.APP_SETTINGS_PREFS, Consts.IS_DB_NO_EMPTY, false)) {
            toolbarSearch.setVisibility(View.GONE);
            btnMainInventory.setEnabled(true);
            btnMainUploadFile.setEnabled(true);
        } else {
            toolbarSearch.setVisibility(View.GONE);
            btnMainInventory.setEnabled(false);
            btnMainUploadFile.setEnabled(false);
        }
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
