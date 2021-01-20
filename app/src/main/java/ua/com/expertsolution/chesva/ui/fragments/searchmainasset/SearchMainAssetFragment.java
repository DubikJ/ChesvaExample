package ua.com.expertsolution.chesva.ui.fragments.searchmainasset;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.adapter.MainAssetListAdapter;
import ua.com.expertsolution.chesva.db.DBConstant;
import ua.com.expertsolution.chesva.model.LoadListEventHandler;
import ua.com.expertsolution.chesva.model.dto.MainAsset;
import ua.com.expertsolution.chesva.scanner.DevBeep;
import ua.com.expertsolution.chesva.scanner.TagProximator;
import ua.com.expertsolution.chesva.ui.activities.MainActivity;
import ua.com.expertsolution.chesva.ui.fragments.FragmentInventoryCallback;
import ua.com.expertsolution.chesva.ui.fragments.FragmentInventoryListener;
import ua.com.expertsolution.chesva.ui.fragments.FragmentOnBackPressed;
import ua.com.expertsolution.chesva.ui.fragments.FragmentOnDispatchTouchEvent;
import ua.com.expertsolution.chesva.ui.widgets.FilteredInstantAutoComplete;
import ua.com.expertsolution.chesva.ui.widgets.ShowHideRecyclerScrollListener;
import ua.com.expertsolution.chesva.utils.ActivityUtils;
import ua.com.expertsolution.chesva.utils.DeviceUtils;
import ua.com.expertsolution.chesva.utils.SharedStorage;

import static ua.com.expertsolution.chesva.common.Consts.APP_SETTINGS_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_INVENTORY;
import static ua.com.expertsolution.chesva.common.Consts.UHF_POWER;
import static ua.com.expertsolution.chesva.common.Consts.UHF_POWER_MAX;
import static ua.com.expertsolution.chesva.common.Consts.UHF_POWER_MIN;

public class SearchMainAssetFragment extends Fragment implements View.OnClickListener, FragmentOnBackPressed,
        FragmentInventoryListener, FragmentInventoryCallback, FragmentOnDispatchTouchEvent {
    private final static int MIN_LIST_COUNT = 50;
    private static final long TAG_TIME_OUT = 1000;

    @BindView(R.id.findPerson)
    FilteredInstantAutoComplete findPerson;

    @BindView(R.id.findModel)
    FilteredInstantAutoComplete findModel;

    @BindView(R.id.findName)
    FilteredInstantAutoComplete findName;

    @BindView(R.id.findBox)
    FilteredInstantAutoComplete findBox;

    @BindView(R.id.btnSearch)
    Button btnSearch;

    @BindView(R.id.rvList)
    RecyclerView rvList;

    @BindView(R.id.pbLoadList)
    ProgressBar pbLoadList;

    IndicatorSeekBar sbPowerRfid;
    ImageView ivSelectedStatus;
    TextView tvSelectedStatus;
    TextView tvSelectedName;
    TextView tvSelectedComment;
    ProgressBar pbRssi;
    Button btnStartStopRssi;
    TextView tvRssi;

    private View view;
    private SearchMainAssetViewModel mViewModel;
    private MainAssetListAdapter mainAssetListAdapter;
    private Boolean startScan = false;
    private Boolean startLoad = false;
    private Boolean startSave = false;
    private Boolean closing = false;
    private Boolean blockPagination = false;
    private int startIdDB = 0;
    private long tagLastSeen = 0;
    private BottomSheetDialog bottomSheetDialog;
    private MainAsset selectedMainAsset;
    private int sbPowerProgress = 0;
    private String deviceName;

    public static SearchMainAssetFragment newInstance() {
        return new SearchMainAssetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search_main_asset, container, false);
        ButterKnife.bind(this, view);
        mViewModel = new ViewModelProvider(this).get(SearchMainAssetViewModel.class);
        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).changeToolbar(getActivity().getString(R.string.main_asset_searching_operation),
                false, true, false);
        ((MainActivity)getActivity()).setFragmentRfidCallBack(this);
        DevBeep.init(getContext());
        initBottomSheetDialog();
        deviceName = DeviceUtils.getDeviceName();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DevBeep.init(getContext());
        btnSearch.setOnClickListener(this);
        btnStartStopRssi.setOnClickListener(this);

        mViewModel.getResultList().observe(getViewLifecycleOwner(), newResult -> {
            switch (newResult.getStatus()) {
                case LoadListEventHandler.LOAD_STARTED:
                    pbLoadList.setVisibility(View.VISIBLE);
                    startLoad = true;
                    break;
                case LoadListEventHandler.LOAD_ERROR:
                    pbLoadList.setVisibility(View.GONE);
                    startLoad = false;
                    break;
                case LoadListEventHandler.LOAD_FINISH:
                    if(newResult.getMainAssets().size()<MIN_LIST_COUNT){
                        blockPagination = true;
                    }else{
                        blockPagination = false;
                    }
                    mainAssetListAdapter.addList(newResult.getMainAssets().size()>MIN_LIST_COUNT+1
                            ? newResult.getMainAssets().subList(0, MIN_LIST_COUNT): newResult.getMainAssets());
                    pbLoadList.setVisibility(View.GONE);
                    startLoad = false;
                    break;
            }
        });

        mViewModel.getResultFilter().observe(getViewLifecycleOwner(), newFilters -> {
            if(newFilters == null){
                return;
            }
            switch (newFilters.getColumn()) {
                case DBConstant.MAIN_ASSET_PERSON_NAME:
                    findPerson.setFilterList(newFilters.getFilterData());
                    break;
                case DBConstant.MAIN_ASSET_MODEL_NAME:
                    findModel.setFilterList(newFilters.getFilterData());
                    break;
                case DBConstant.MAIN_ASSET_NAME:
                    findName.setFilterList(newFilters.getFilterData());
                    break;
                case DBConstant.MAIN_ASSET_BOX_NAME:
                    findBox.setFilterList(newFilters.getFilterData());
                    break;
            }
        });

        findPerson.init(getActivity(), null, new FilteredInstantAutoComplete.CallBackListener() {
            @Override
            public void afterTextChanged(Editable editable) { }

            @Override
            public void startLoadData(Editable editable) {
                mViewModel.loadFilters(DBConstant.MAIN_ASSET_PERSON_NAME, editable.toString());
            }

            @Override
            public void onSelectItem(Editable editable, Object o, Editable editableMoreOne) {
                findPerson.clearFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findPerson.getWindowToken(), 0);
            }

            @Override
            public void onPressRightButton() {
                findPerson.setFilterList(new ArrayList<>());
                findPerson.setText("");
            }

        });

        findModel.init(getActivity(), null, new FilteredInstantAutoComplete.CallBackListener() {
            @Override
            public void afterTextChanged(Editable editable) { }

            @Override
            public void startLoadData(Editable editable) {
                mViewModel.loadFilters(DBConstant.MAIN_ASSET_MODEL_NAME, editable.toString());
            }

            @Override
            public void onSelectItem(Editable editable, Object o, Editable editableMoreOne) {
                findModel.clearFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findModel.getWindowToken(), 0);
            }

            @Override
            public void onPressRightButton() {
                findModel.setFilterList(new ArrayList<>());
                findModel.setText("");
            }

        });

        findName.init(getActivity(), null, new FilteredInstantAutoComplete.CallBackListener() {
            @Override
            public void afterTextChanged(Editable editable) { }

            @Override
            public void startLoadData(Editable editable) {
                mViewModel.loadFilters(DBConstant.MAIN_ASSET_NAME, editable.toString());
            }

            @Override
            public void onSelectItem(Editable editable, Object o, Editable editableMoreOne) {
                findName.clearFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findName.getWindowToken(), 0);
            }

            @Override
            public void onPressRightButton() {
                findName.setFilterList(new ArrayList<>());
                findName.setText("");
            }

        });

        findBox.init(getActivity(), null, new FilteredInstantAutoComplete.CallBackListener() {
            @Override
            public void afterTextChanged(Editable editable) { }

            @Override
            public void startLoadData(Editable editable) {
                mViewModel.loadFilters(DBConstant.MAIN_ASSET_BOX_NAME, editable.toString());
            }

            @Override
            public void onSelectItem(Editable editable, Object o, Editable editableMoreOne) {
                findBox.clearFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findBox.getWindowToken(), 0);
            }

            @Override
            public void onPressRightButton() {
                findBox.setFilterList(new ArrayList<>());
                findBox.setText("");
            }

        });

        mainAssetListAdapter = new MainAssetListAdapter(getActivity(), false, new MainAssetListAdapter.AdapterListener() {
            @Override
            public void onItemClick(int position, View v) {
                if(startScan){
                    Toast.makeText(getActivity(),
                            getActivity().getString(R.string.wait_end_procces_inventory),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedMainAsset = mainAssetListAdapter.getItem(position);
                if(selectedMainAsset!=null){
                    showBottomSheetDialog();
                }
            }

            @Override
            public boolean onLongItemClick(int position, View v) { return true; }

        });
        rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvList.setAdapter(mainAssetListAdapter);

        rvList.addOnScrollListener(new ShowHideRecyclerScrollListener() {
            @Override
            public void show() {
            }

            @Override
            public void hide() {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();

                int totalItemCount = lm.getItemCount();
                int lastVisibleItem = lm
                        .findLastVisibleItemPosition();
                if (totalItemCount <= (lastVisibleItem + 1) && !startLoad && !blockPagination) {
                    pbLoadList.setVisibility(View.VISIBLE);
                    try {
                        try {
                            startIdDB = mainAssetListAdapter.getList().get(mainAssetListAdapter.getList().size() - 1).getId()+1;
                        } catch (Exception e) {
                            startIdDB = 0;
                        }
                        mViewModel.getMainAssetList(startIdDB, MIN_LIST_COUNT, findPerson.getText().toString(),
                                findModel.getText().toString(), findName.getText().toString(), findBox.getText().toString());
                    } catch (Exception e) {

                    }
                    if (!startLoad) {
                        pbLoadList.setVisibility(View.GONE);
                    }
                }
            }
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
        if(startScan){
            ActivityUtils.showShortToast(getActivity(), getString(R.string.wait_end_procces_inventory));
            return false;
        }
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
            case R.id.btnSearch:
                startIdDB = 0;
                mainAssetListAdapter.clearList();
                mViewModel.getMainAssetList(startIdDB, MIN_LIST_COUNT, findPerson.getText().toString(),
                        findModel.getText().toString(), findName.getText().toString(), findBox.getText().toString());
                break;
            case R.id.btnStartStopRssi:
                if (startScan) {
                    ((MainActivity) getActivity()).stopRfidScan();
                    onInventoryStopped();
                } else {
                    ((MainActivity) getActivity()).startRfidScan();
                    onInventoryStarted();
                }
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if(startScan){
            ActivityUtils.showShortToast(getActivity(), getString(R.string.wait_end_procces_inventory));
            return true;
        }
        return false;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (startScan) {
            ((MainActivity) getActivity()).stopRfidScan();
            onInventoryStopped();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.closing = true;
        ((MainActivity) getActivity()).setMaskScan(null);
    }

    @Override
    public void dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            try {
                View v = getActivity().getCurrentFocus();
                if (v instanceof EditText) {
                    Rect outRect = new Rect();
                    v.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        v.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }catch (Exception e){
                Log.e(TAGLOG_INVENTORY, e.toString());
            }
        }
    }

    @Override
    public void onTagRead(String epc, String rssi) {
        DevBeep.PlayOK();
        if (selectedMainAsset == null || !selectedMainAsset.getRfid().equals(epc)) {
            return;
        }
        tagLastSeen = System.currentTimeMillis();
        String RFIDtag = epc;
        double rssiDouble = 0;
        try{
            rssiDouble = Double.valueOf(rssi.replaceAll(",","."));
        }catch (Exception e){
            try{
                rssiDouble = Double.valueOf(rssi);
            }catch (Exception ex){
            }
        }
        TagProximator.addData(RFIDtag, rssiDouble);
        double normalizeRssi = (double) TagProximator.getProximity(RFIDtag);
        int range = TagProximator.getScaledProximity(RFIDtag, deviceName);
        final String tagRssi = String.format("%1$,.1f", new Object[]{Double.valueOf(normalizeRssi)});
        range = Math.min(range, 100);
        range = Math.max(range, 0);
        final int finalRange = range;
        getActivity().runOnUiThread(() -> {
            pbRssi.setProgress(finalRange);
            tvRssi.setText(tagRssi);
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((MainActivity) getActivity()).setMaskScan(null);
    }

    @Override
    public void startInventory() {
        onInventoryStarted();
    }

    @Override
    public void stopInventory() {
        onInventoryStopped();
    }

    private void onInventoryStarted(){
        startSoundFeedbackThread();
        startScan = true;
        btnStartStopRssi.setSelected(true);
        btnStartStopRssi.setText(getString(R.string.stop));
    }

    private void onInventoryStopped(){
        startScan = false;
        btnStartStopRssi.setSelected(false);
        btnStartStopRssi.setText(getString(R.string.start));
        closing = true;
        tvRssi.setText("0");
        pbRssi.setProgress(0);
    }

    private void initBottomSheetDialog() {
        View view = getLayoutInflater().inflate(R.layout.fragment_search_bottom, null);
        bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setOnCancelListener(dialogInterface -> {
            if (startScan) {
                ((MainActivity) getActivity()).stopRfidScan();
                onInventoryStopped();
                ((MainActivity) getActivity()).setMaskScan(null);
            }
        });

        bottomSheetDialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
            switch (keyEvent.getAction()){
                case KeyEvent.ACTION_DOWN:
                    if(!startScan) {
                        if (i == 293 || i == KeyEvent.KEYCODE_F9) {
                            ((MainActivity) getActivity()).startRfidScan();
                            onInventoryStarted();
                        }
                    }
                    break;
                case KeyEvent.ACTION_UP:
                    if (i == 293 || i == KeyEvent.KEYCODE_F9) {
                        ((MainActivity) getActivity()).stopRfidScan();
                        onInventoryStopped();
                    }
                    break;
            }
            return true;
        });

        sbPowerRfid = view.findViewById(R.id.seekPowerScanner);
        ivSelectedStatus = view.findViewById(R.id.ivStatus);
        tvSelectedStatus = view.findViewById(R.id.tvStatus);
        tvSelectedName = view.findViewById(R.id.tvName);
        tvSelectedComment = view.findViewById(R.id.tvComment);
        btnStartStopRssi = view.findViewById(R.id.btnStartStopRssi);
        pbRssi = view.findViewById(R.id.pbRssi);
        tvRssi = view.findViewById(R.id.tvRssi);
        int savedPwrMax = SharedStorage.getInteger(getActivity(), APP_SETTINGS_PREFS, UHF_POWER_MAX, 30);
        int savedPwr = SharedStorage.getInteger(getActivity(), APP_SETTINGS_PREFS, UHF_POWER, 0);
        int savedPwrMin = SharedStorage.getInteger(getActivity(), APP_SETTINGS_PREFS, UHF_POWER_MIN, 0);
        if (savedPwr < savedPwrMin) savedPwr = savedPwrMin;

        sbPowerRfid.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                sbPowerProgress = Integer.valueOf((seekBar.getProgress() * savedPwrMax) / 100);
                if(startScan){
                    ActivityUtils.showShortToast(getActivity(), getString(R.string.wait_end_procces_inventory));
                    seekBar.setProgress(Integer.valueOf(( SharedStorage.getInteger(getActivity(), APP_SETTINGS_PREFS, UHF_POWER, sbPowerProgress) * 100) / savedPwrMax));
                }else {
                    if (sbPowerProgress < 5) {
                        sbPowerProgress = 5;
                        seekBar.setProgress(Integer.valueOf((sbPowerProgress * 100) / savedPwrMax));
                    }
                    if(((MainActivity) getActivity()).getUnfScanner().setPowerRFID(sbPowerProgress)) {
                        SharedStorage.setInteger(getActivity(), APP_SETTINGS_PREFS, UHF_POWER, sbPowerProgress);
                    }else {
                        ActivityUtils.showShortToast(getActivity(), getString(R.string.reader_power_application_error));
                        seekBar.setProgress(Integer.valueOf((SharedStorage.getInteger(getActivity(),
                                APP_SETTINGS_PREFS, UHF_POWER, sbPowerProgress) * 100) / savedPwrMax));
                    }
                }
            }
        });

        sbPowerRfid.setProgress(Integer.valueOf((savedPwr * 100)/savedPwrMax));
        final int finalSavedPwr = savedPwr;

        sbPowerRfid.post(() -> sbPowerRfid.setProgress(Integer.valueOf((finalSavedPwr * 100)/savedPwrMax)));
    }

    private void showBottomSheetDialog() {
        if(bottomSheetDialog!=null && selectedMainAsset!=null){
            if(TextUtils.isEmpty(selectedMainAsset.getRfid())){
                ActivityUtils.showShortToast(getActivity(), getString(R.string.main_asset_not_have_rfid));
                return;
            }
            if (!((MainActivity) getActivity()).setMaskScan(selectedMainAsset.getRfid())){
                ActivityUtils.showShortToast(getActivity(), getString(R.string.main_asset_search_bad_rfid));
                return;
            }
            tvSelectedName.setText(selectedMainAsset.getName());
            tvSelectedStatus.setText(selectedMainAsset.getModelName());
            tvSelectedComment.setText(selectedMainAsset.getRfid());
            if(!TextUtils.isEmpty(selectedMainAsset.getRfid())){
                Picasso.get()
                        .load(R.drawable.ic_check_white).placeholder(R.drawable.ic_check_white)
                        .into(ivSelectedStatus);
            }else{
                Picasso.get()
                        .load(R.drawable.ic_close_white).placeholder(R.drawable.ic_close_white)
                        .into(ivSelectedStatus);
            }
            tvRssi.setText("0");
            pbRssi.setProgress(0);
            bottomSheetDialog.show();
        }
    }

    private void startSoundFeedbackThread() {
        new Thread(() -> {
            while (!closing) {
                refreshDetection();
            }
            getActivity().runOnUiThread(() -> {
                tvRssi.setText("0");
                pbRssi.setProgress(0);
            });
        }).start();
    }

    public void refreshDetection() {
        if (System.currentTimeMillis() - tagLastSeen > TAG_TIME_OUT && !tvRssi.getText().equals("0")) {
            getActivity().runOnUiThread(() -> {
                tvRssi.setText("0");
                pbRssi.setProgress(0);
            });
        }
    }
}