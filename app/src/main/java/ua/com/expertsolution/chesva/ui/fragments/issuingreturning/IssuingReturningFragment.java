package ua.com.expertsolution.chesva.ui.fragments.issuingreturning;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.util.ArrayList;
import java.util.Date;

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
import ua.com.expertsolution.chesva.adapter.OperationListAdapter;
import ua.com.expertsolution.chesva.adapter.PersonAutoCompleteAdapter;
import ua.com.expertsolution.chesva.db.DBConstant;
import ua.com.expertsolution.chesva.model.LoadItemEventHandler;
import ua.com.expertsolution.chesva.model.LoadListEventHandler;
import ua.com.expertsolution.chesva.model.dto.MainAsset;
import ua.com.expertsolution.chesva.model.dto.Operation;
import ua.com.expertsolution.chesva.model.dto.Person;
import ua.com.expertsolution.chesva.scanner.DevBeep;
import ua.com.expertsolution.chesva.ui.activities.MainActivity;
import ua.com.expertsolution.chesva.ui.fragments.FragmentInventoryCallback;
import ua.com.expertsolution.chesva.ui.fragments.FragmentInventoryListener;
import ua.com.expertsolution.chesva.ui.fragments.FragmentOnBackPressed;
import ua.com.expertsolution.chesva.ui.fragments.FragmentOnDispatchTouchEvent;
import ua.com.expertsolution.chesva.ui.widgets.FilteredInstantAutoComplete;
import ua.com.expertsolution.chesva.ui.widgets.ShowHideRecyclerScrollListener;
import ua.com.expertsolution.chesva.utils.ActivityUtils;
import ua.com.expertsolution.chesva.utils.NumberUtils;
import ua.com.expertsolution.chesva.utils.SharedStorage;

import static ua.com.expertsolution.chesva.common.Consts.APP_SETTINGS_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.DATE_SYNC_FORMAT;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_INVENTORY;
import static ua.com.expertsolution.chesva.common.Consts.UHF_POWER;
import static ua.com.expertsolution.chesva.common.Consts.UHF_POWER_MAX;
import static ua.com.expertsolution.chesva.common.Consts.UHF_POWER_MIN;

public class IssuingReturningFragment extends Fragment implements View.OnClickListener, FragmentOnBackPressed,
        FragmentInventoryListener, FragmentInventoryCallback, FragmentOnDispatchTouchEvent {
    private static final String IS_RETURNED = "is_returned";
    private final static int MIN_LIST_COUNT = 50;

    @BindView(R.id.seekPowerScanner)
    IndicatorSeekBar sbPowerRfid;

    @BindView(R.id.btnFindPerson)
    Button btnFindPerson;

    @BindView(R.id.btnFindMainAsset)
    Button btnFindMainAsset;

    @BindView(R.id.rvList)
    RecyclerView rvList;

    @BindView(R.id.pbLoadList)
    ProgressBar pbLoadList;

    @BindView(R.id.findPerson)
    FilteredInstantAutoComplete viewFindPerson;

    @BindView(R.id.tvlistCount)
    TextView tvlistCount;

    @BindView(R.id.tvlistCountName)
    TextView tvlistCountName;

    private View view;
    private IssuingReturningViewModel mViewModel;
    private MainAssetListAdapter listAdapter;
    private Boolean startScanPerson = false;
    private Boolean startScanMainAsset = false;
    private Boolean startLoad = false;
    private Boolean startSave = false;
    public boolean blockPagination = false;
    private int startIdDB = 0;
    private int sbPowerProgress = 0;
    private Person foundedPerson;
    private String scannedRfid;
    private boolean isReturned;

    public static IssuingReturningFragment getInstance(boolean isReturned) {

        Bundle args = new Bundle();
        args.putBoolean(IS_RETURNED, isReturned);
        IssuingReturningFragment fragment = new IssuingReturningFragment();
        fragment.setArguments(args);
        return  fragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            isReturned = bundle.getBoolean(IS_RETURNED);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_issuing_returning, container, false);
        ButterKnife.bind(this, view);
        readBundle(getArguments());
        mViewModel = new ViewModelProvider(this).get(IssuingReturningViewModel.class);
        mViewModel.setActivityContext(getActivity());
        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).changeToolbar(isReturned ? getActivity().getString(R.string.main_asset_returning_operation) :
                        getActivity().getString(R.string.main_asset_issuing_operation),
                false, true, false);
        ((MainActivity)getActivity()).setFragmentRfidCallBack(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DevBeep.init(getContext());
        btnFindPerson.setOnClickListener(this);
        btnFindMainAsset.setOnClickListener(this);

        tvlistCountName.setText(isReturned ? getString(R.string.returned_name) : getString(R.string.issued_name));
        mViewModel.getResultStatus().observe(getViewLifecycleOwner(), newResult -> {
            if(newResult!=null) {
                tvlistCount.setText(String.valueOf(newResult.getFound()));
            }
        });

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
                    if(newResult.getOperations().size()<MIN_LIST_COUNT){
                        blockPagination = true;
                    }else{
                        blockPagination = false;
                    }
                    listAdapter.addList(newResult.getMainAssets().size()>MIN_LIST_COUNT+1
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
                case DBConstant.PERSON_FULL_NAME:
                    viewFindPerson.setFilterList(newFilters.getFilterData());
                    break;
            }
        });

        mViewModel.getResultSave().observe(getViewLifecycleOwner(), newSaved -> {
            if(newSaved == null){
                return;
            }
            switch (newSaved.getStatus()) {
                case LoadItemEventHandler.LOAD_STARTED:
                    ((MainActivity)getActivity()).showDialogLoad(null, null);
                    break;
                case LoadItemEventHandler.LOAD_ERROR:
                    startSave = false;
                    ((MainActivity)getActivity()).cancelDialogLoad();
                    ActivityUtils.showMessage(getActivity(), null, null,
                            getString(R.string.not_saved) +
                                    (TextUtils.isEmpty(newSaved.getTextError()) ? "" : ": "+newSaved.getTextError()));
                    break;
                case LoadItemEventHandler.LOAD_FINISH:
                    startSave = false;
                    ((MainActivity)getActivity()).cancelDialogLoad();
                    ActivityUtils.showShortToast(getActivity(), getString(R.string.saved));
//                    clearData();
                    startIdDB = 0;
                    listAdapter.clearList();
                    if(foundedPerson!=null) {
                        mViewModel.updateStatuses(foundedPerson.getId(), isReturned);
                        mViewModel.getMainAssetList(startIdDB, MIN_LIST_COUNT, foundedPerson.getId(), isReturned);
                    }
                    break;
            }
        });


        viewFindPerson.init(getActivity(), new PersonAutoCompleteAdapter(getActivity()), new FilteredInstantAutoComplete.CallBackListener() {
            @Override
            public void afterTextChanged(Editable editable) { }

            @Override
            public void startLoadData(Editable editable) {
                mViewModel.loadFilters(DBConstant.PERSON_FULL_NAME, editable.toString());
            }

            @Override
            public void onSelectItem(Editable editable, Object o, Editable editableMoreOne) {
                viewFindPerson.clearFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(viewFindPerson.getWindowToken(), 0);
                ((MainActivity) getActivity()).stopRfidScan();
                onFoundPerson((Person) o);
                onInventoryStopped();
            }

            @Override
            public void onPressRightButton() {
                clearData();
            }

        });

        listAdapter = new MainAssetListAdapter(getActivity(), true, new MainAssetListAdapter.AdapterListener() {
            @Override
            public void onItemClick(int position, View v) { }

            @Override
            public boolean onLongItemClick(int position, View v) {

                if(isReturned) return true;

                ActivityUtils.showQuestion(getActivity(), getString(R.string.attention), null,
                        getString(R.string.question_delete_tool),
                        getString(R.string.questions_answer_yes),
                        getString(R.string.questions_answer_no), null, new ActivityUtils.QuestionAnswer() {
                            @Override
                            public void onPositiveAnswer() {

                                MainAsset mainAsset = listAdapter.getItem(position);
                                mViewModel.saveOperation(Operation.builder()
                                        .typeOperation(Operation.TYPE_OPERATION_RETURNING_MAIN_ASSET)
                                        .idOwner(mainAsset.getId())
                                        .ownerName(mainAsset.getName())
                                        .personID(-1)
                                        .personName("")
                                        .rfid(mainAsset.getRfid())
                                        .conditionID(mainAsset.getConditionID())
                                        .modelName(mainAsset.getModelName())
                                        .repairComment(mainAsset.getComment())
                                        .edited(DATE_SYNC_FORMAT.format(new Date()))
                                        .timeEdit(new Date().getTime())
                                        .tempId(NumberUtils.generateUniqueId())
                                        .send(1).build(), foundedPerson);

                            }

                            @Override
                            public void onNegativeAnswer() {
                            }

                            @Override
                            public void onNeutralAnswer() {
                            }
                        });
                return true;
            }

        });
        rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvList.setAdapter(listAdapter);

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
                            startIdDB = listAdapter.getList().get(listAdapter.getList().size() - 1).getId()+1;
                        } catch (Exception e) {
                            startIdDB = 0;
                        }
                        if(foundedPerson!=null) {
                            mViewModel.getMainAssetList(startIdDB, MIN_LIST_COUNT, foundedPerson.getId(), isReturned);
                        }
                    } catch (Exception e) {

                    }
                    if (!startLoad) {
                        pbLoadList.setVisibility(View.GONE);
                    }
                }
            }
        });

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
                if(startScanPerson || startScanMainAsset){
                    ActivityUtils.showShortToast(getActivity(), getString(R.string.wait_end_procces_inventory));
                    seekBar.setProgress(Integer.valueOf(( SharedStorage.getInteger(getActivity(),
                            APP_SETTINGS_PREFS, UHF_POWER, sbPowerProgress) * 100) / savedPwrMax));
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

        setEnabledButton();
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
        if(startScanPerson || startScanMainAsset){
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
            case R.id.btnFindPerson:
                if (startScanPerson) {
                    ((MainActivity) getActivity()).stopRfidScan();
                    onInventoryStopped();
                } else {
                    ((MainActivity) getActivity()).startRfidScan();
                    startScanPerson = true;
                    onInventoryStarted();
                }
                break;
            case R.id.btnFindMainAsset:
                if (startScanMainAsset) {
                    ((MainActivity) getActivity()).stopRfidScan();
                    onInventoryStopped();
                } else {
                    ((MainActivity) getActivity()).startRfidScan();
                    startScanMainAsset = true;
                    onInventoryStarted();
                }
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if(startScanPerson || startScanMainAsset){
            ActivityUtils.showShortToast(getActivity(), getString(R.string.wait_end_procces_inventory));
            return true;
        }
        return false;
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
    public void onTagRead(String rfidCode, String rssi) {
        DevBeep.PlayOK();
        if(TextUtils.isEmpty(scannedRfid) || !scannedRfid.equals(rfidCode)) {
            scannedRfid = rfidCode;
            getActivity().runOnUiThread(() -> {
                ((MainActivity) getActivity()).setBlockRfidScan();
                ((MainActivity) getActivity()).stopRfidScan();
                if(startScanPerson) {
                    mViewModel.getPersonByRfid(scannedRfid).observe(getViewLifecycleOwner(), result -> {
                        if (result != null) {
                            onFoundPerson(result);
                        }else{
                            ActivityUtils.showShortToast(getActivity(), getString(R.string.person_by_rfid_not_found));
                        }
                    });
                }
                if(startScanMainAsset) {
                    mViewModel.getMainAssetByRfid(scannedRfid).observe(getViewLifecycleOwner(), result -> {
                        if (result != null) {
                            if (!startSave) {
                                startSave = true;
                                mViewModel.saveOperation(Operation.builder()
                                        .typeOperation(isReturned ? Operation.TYPE_OPERATION_RETURNING_MAIN_ASSET:
                                                Operation.TYPE_OPERATION_ISSUING_MAIN_ASSET)
                                        .idOwner(result.getId())
                                        .ownerName(result.getName())
                                        .personID(isReturned? -1 : foundedPerson.getId())
                                        .personName(isReturned? "" : foundedPerson.getFullName())
                                        .rfid(result.getRfid())
                                        .conditionID(result.getConditionID())
                                        .modelName(result.getModelName())
                                        .repairComment(result.getComment())
                                        .edited(DATE_SYNC_FORMAT.format(new Date()))
                                        .timeEdit(new Date().getTime())
                                        .tempId(NumberUtils.generateUniqueId())
                                        .send(1).build(), foundedPerson);
                            }
                        }else{
                            ActivityUtils.showShortToast(getActivity(), getString(R.string.main_asset_by_rfid_not_found));
                        }
                    });
                }
                onInventoryStopped();
            });
        }
    }

    private void onFoundPerson(Person person){
        foundedPerson = person;
        setEnabledButton();
        viewFindPerson.setText(person.getFullName(), false);
        tvlistCount.setText("0");
        listAdapter.clearList();
        mViewModel.updateStatuses(foundedPerson.getId(), isReturned);
        mViewModel.getMainAssetList(startIdDB, MIN_LIST_COUNT, foundedPerson.getId(), isReturned);
    }

    private void clearData(){
        foundedPerson = null;
        setEnabledButton();
        viewFindPerson.setFilterList(new ArrayList<>());
        viewFindPerson.setText("");
        tvlistCount.setText("0");
        listAdapter.clearList();
    }

    private void setEnabledButton(){

        if(foundedPerson==null){
            btnFindPerson.setEnabled(true);
            btnFindMainAsset.setEnabled(false);
        }else{
            btnFindPerson.setEnabled(false);
            btnFindMainAsset.setEnabled(true);
        }
    }

    @Override
    public void startInventory() {
        if(!startScanPerson && !startScanMainAsset){
            if(foundedPerson!=null){
                startScanMainAsset = true;
            }else{
                startScanPerson = true;
            }
        }
        onInventoryStarted();
    }

    @Override
    public void stopInventory() {
        onInventoryStopped();
    }

    private void onInventoryStarted(){
        if(startScanPerson){
            btnFindPerson.setSelected(true);
            btnFindPerson.setText(getString(R.string.stop_scan));
            viewFindPerson.setEditable(false);
            btnFindMainAsset.setEnabled(false);
        }
        if(startScanMainAsset){
            btnFindMainAsset.setSelected(true);
            btnFindMainAsset.setText(getString(R.string.stop_scan));
            viewFindPerson.setEditable(false);
            btnFindPerson.setEnabled(false);
        }
    }

    private void onInventoryStopped(){
        scannedRfid = "";
        startScanPerson = false;
        startScanMainAsset = false;
        viewFindPerson.setEditable(true);
        btnFindPerson.setEnabled(true);
        btnFindPerson.setSelected(false);
        btnFindPerson.setText(getString(R.string.find_person));
        btnFindMainAsset.setEnabled(true);
        btnFindMainAsset.setSelected(false);
        btnFindMainAsset.setText(getString(R.string.find_main_asset));
        setEnabledButton();
    }
}