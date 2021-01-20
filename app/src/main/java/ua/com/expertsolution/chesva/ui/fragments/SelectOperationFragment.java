package ua.com.expertsolution.chesva.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.ui.activities.MainActivity;
import ua.com.expertsolution.chesva.ui.fragments.boxAddRfid.BoxAddRfidFragment;
import ua.com.expertsolution.chesva.ui.fragments.issuingreturning.IssuingReturningFragment;
import ua.com.expertsolution.chesva.ui.fragments.issuingreturning.IssuingReturningViewModel;
import ua.com.expertsolution.chesva.ui.fragments.mainAssetAddRfid.MainAssetAddRfidFragment;
import ua.com.expertsolution.chesva.ui.fragments.mainassetinbox.MainAssetInBoxFragment;
import ua.com.expertsolution.chesva.ui.fragments.searchmainasset.SearchMainAssetFragment;


public class SelectOperationFragment extends Fragment implements View.OnClickListener, FragmentOnBackPressed {

    @BindView(R.id.btnBoxAddRfid)
    Button btnBoxAddRfid;

    @BindView(R.id.btnMainAssetAddRfid)
    Button btnMainAssetAddRfid;

    @BindView(R.id.btnAssembleBox)
    Button btnAssembleBox;

    @BindView(R.id.btnMainAssetIssuing)
    Button btnMainAssetIssuing;

    @BindView(R.id.btnMainAssetReturning)
    Button btnMainAssetReturning;

    @BindView(R.id.btnMainAssetSearch)
    Button btnMainAssetSearch;

    private View view;
    private Menu menu;

    public static SelectOperationFragment getInstance() {

        Bundle args = new Bundle();
        SelectOperationFragment fragment = new SelectOperationFragment();
        fragment.setArguments(args);
        return  fragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_operation, container, false);
        ButterKnife.bind(this, view);
        readBundle(getArguments());

        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).changeToolbar(
                getActivity().getString(R.string.work_with_instrument), false, true, false);
        ((MainActivity)getActivity()).setFragmentRfidCallBack(null);

        btnBoxAddRfid.setOnClickListener(this);
        btnMainAssetAddRfid.setOnClickListener(this);
        btnAssembleBox.setOnClickListener(this);
        btnMainAssetIssuing.setOnClickListener(this);
        btnMainAssetReturning.setOnClickListener(this);
        btnMainAssetSearch.setOnClickListener(this);

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
            case R.id.btnBoxAddRfid:
                ((MainActivity) getActivity()).showNextFragment(BoxAddRfidFragment.newInstance());
                break;
            case R.id.btnMainAssetAddRfid:
                ((MainActivity) getActivity()).showNextFragment(MainAssetAddRfidFragment.newInstance());
                break;
            case R.id.btnAssembleBox:
                ((MainActivity) getActivity()).showNextFragment(MainAssetInBoxFragment.getInstance());
                break;
            case R.id.btnMainAssetIssuing:
                ((MainActivity) getActivity()).showNextFragment(IssuingReturningFragment.getInstance(false));
                break;
            case R.id.btnMainAssetReturning:
                ((MainActivity) getActivity()).showNextFragment(IssuingReturningFragment.getInstance(true));
                break;
            case R.id.btnMainAssetSearch:
                ((MainActivity) getActivity()).showNextFragment(SearchMainAssetFragment.newInstance());
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
