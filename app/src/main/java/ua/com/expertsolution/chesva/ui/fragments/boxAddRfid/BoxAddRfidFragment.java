package ua.com.expertsolution.chesva.ui.fragments.boxAddRfid;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.adapter.BoxAutoCompleteAdapter;
import ua.com.expertsolution.chesva.adapter.BoxListAdapter;
import ua.com.expertsolution.chesva.db.DBConstant;
import ua.com.expertsolution.chesva.model.LoadItemEventHandler;
import ua.com.expertsolution.chesva.model.LoadListEventHandler;
import ua.com.expertsolution.chesva.model.dto.Box;
import ua.com.expertsolution.chesva.scanner.DevBeep;
import ua.com.expertsolution.chesva.ui.activities.MainActivity;
import ua.com.expertsolution.chesva.ui.fragments.FragmentInventoryListener;
import ua.com.expertsolution.chesva.ui.fragments.FragmentInventoryCallback;
import ua.com.expertsolution.chesva.ui.fragments.FragmentOnBackPressed;
import ua.com.expertsolution.chesva.ui.fragments.FragmentOnDispatchTouchEvent;
import ua.com.expertsolution.chesva.ui.widgets.FilteredInstantAutoComplete;
import ua.com.expertsolution.chesva.ui.widgets.ShowHideRecyclerScrollListener;
import ua.com.expertsolution.chesva.utils.ActivityUtils;
import ua.com.expertsolution.chesva.utils.NumberUtils;
import ua.com.expertsolution.chesva.utils.SharedStorage;

import static ua.com.expertsolution.chesva.common.Consts.APP_SETTINGS_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG_INVENTORY;
import static ua.com.expertsolution.chesva.common.Consts.UHF_POWER;
import static ua.com.expertsolution.chesva.common.Consts.UHF_POWER_MAX;
import static ua.com.expertsolution.chesva.common.Consts.UHF_POWER_MIN;

public class BoxAddRfidFragment extends Fragment implements View.OnClickListener, FragmentOnBackPressed,
        FragmentInventoryListener, FragmentInventoryCallback, FragmentOnDispatchTouchEvent {
    private final static int MIN_LIST_COUNT = 50;

    @BindView(R.id.seekPowerScanner)
    IndicatorSeekBar sbPowerRfid;

    @BindView(R.id.ivInAllListContainer)
    CardView ivInAllListContainer;

    @BindView(R.id.ivWithRfidContainer)
    CardView ivWithRfidContainer;

    @BindView(R.id.ivWithoutRfidContainer)
    CardView ivWithoutRfidContainer;

    @BindView(R.id.tvAllList)
    TextView tvAllList;

    @BindView(R.id.tvWithRfid)
    TextView tvWithRfid;

    @BindView(R.id.tvWithoutRfid)
    TextView tvWithoutRfid;

    @BindView(R.id.tvAllListName)
    TextView tvAllListName;

    @BindView(R.id.tvWithRfidName)
    TextView tvWithRfidName;

    @BindView(R.id.tvWithoutRfidName)
    TextView tvWithoutRfidName;

    @BindView(R.id.ivAllList)
    ImageView ivAllList;

    @BindView(R.id.ivWithRfid)
    ImageView ivWithRfid;

    @BindView(R.id.ivWithoutRfid)
    ImageView ivWithoutRfid;

    @BindView(R.id.btnStartStopRfid)
    Button btnStartStopRfid;

    @BindView(R.id.rvList)
    RecyclerView rvList;

    @BindView(R.id.pbLoadList)
    ProgressBar pbLoadList;

    @BindView(R.id.findBox)
    FilteredInstantAutoComplete fiacFindBox;

    @BindView(R.id.rfidResultContainer)
    CardView rfidResultContainer;

    @BindView(R.id.tvRfidResult)
    TextView tvRfidResult;

    private View view;
    private BoxAddRfidViewModel mViewModel;
    private BoxListAdapter boxListAdapter;
    private Boolean startScan = false;
    private Boolean startLoad = false;
    private Boolean startSave = false;
    public boolean blockPagination = false;
    private int startIdDB = 0;
    private int filterType = BoxAddRfidViewModel.TYPE_FILTER_LIST_ALL;
    private int sbPowerProgress = 0;
    private Box foundedBox;
    private String scannedRfid;

    public static BoxAddRfidFragment newInstance() {
        return new BoxAddRfidFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_box_add_rfid, container, false);
        ButterKnife.bind(this, view);
        mViewModel = new ViewModelProvider(this).get(BoxAddRfidViewModel.class);
        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).changeToolbar(getActivity().getString(R.string.box_add_rfid_title),
                false, true, false);
        ((MainActivity)getActivity()).setFragmentRfidCallBack(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DevBeep.init(getContext());
        ivInAllListContainer.setOnClickListener(this);
        ivWithRfidContainer.setOnClickListener(this);
        ivWithoutRfidContainer.setOnClickListener(this);
        btnStartStopRfid.setOnClickListener(this);

        mViewModel.getResultStatus().observe(getViewLifecycleOwner(), newResult -> {
            if(newResult!=null) {
                tvAllList.setText(String.valueOf(newResult.getInSearchOf()));
                tvWithRfid.setText(String.valueOf(newResult.getFound()));
                tvWithoutRfid.setText(String.valueOf(newResult.getCreated()));
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
                    if(newResult.getBoxList().size()<MIN_LIST_COUNT){
                        blockPagination = true;
                    }else{
                        blockPagination = false;
                    }
                    boxListAdapter.addList(newResult.getBoxList().size()>MIN_LIST_COUNT+1
                            ? newResult.getBoxList().subList(0, MIN_LIST_COUNT): newResult.getBoxList());
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
                case DBConstant.BOX_NAME:
                    fiacFindBox.setFilterList(newFilters.getFilterData());
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
                    mViewModel.updateStatuses();
                    ActivityUtils.showShortToast(getActivity(), getString(R.string.saved));
                    clearData();
                    startIdDB = 0;
                    boxListAdapter.clearList();
                    mViewModel.getBoxList(startIdDB, MIN_LIST_COUNT, filterType);
                    break;
            }
        });


        fiacFindBox.init(getActivity(), new BoxAutoCompleteAdapter(getActivity()), new FilteredInstantAutoComplete.CallBackListener() {
            @Override
            public void afterTextChanged(Editable editable) { }

            @Override
            public void startLoadData(Editable editable) {
                mViewModel.loadFilters(DBConstant.BOX_NAME, editable.toString());
            }

            @Override
            public void onSelectItem(Editable editable, Object o, Editable editableMoreOne) {
                foundedBox = (Box) o;
                setEnabledButton();
                if(!TextUtils.isEmpty(foundedBox.getRfid())) {
                    rfidResultContainer.setVisibility(View.VISIBLE);
                    tvRfidResult.setText(foundedBox.getRfid());
                }else{
                    rfidResultContainer.setVisibility(View.INVISIBLE);
                    tvRfidResult.setText("");
                }
                fiacFindBox.clearFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(fiacFindBox.getWindowToken(), 0);
            }

            @Override
            public void onPressRightButton() {
                clearData();
            }

        });

        boxListAdapter = new BoxListAdapter(getActivity(), new BoxListAdapter.AdapterListener() {
            @Override
            public void onItemClick(int position, View v) { }

            @Override
            public boolean onLongItemClick(int position, View v) { return true; }

        });
        rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvList.setAdapter(boxListAdapter);

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
                            startIdDB = boxListAdapter.getList().get(boxListAdapter.getList().size() - 1).getId()+1;
                        } catch (Exception e) {
                            startIdDB = 0;
                        }
                        mViewModel.getBoxList(startIdDB, MIN_LIST_COUNT, filterType);
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
                if(startScan){
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
        mViewModel.updateStatuses();
        mViewModel.getBoxList(startIdDB, MIN_LIST_COUNT, mViewModel.TYPE_FILTER_LIST_ALL);
        ivInAllListContainer.setCardBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimary));
        tvAllList.setTextAppearance(getActivity(), R.style.TextStyle_Standart_Bold_White);
        tvAllListName.setTextAppearance(getActivity(), R.style.TextStyle_Small_Bold_White);
        Picasso.get()
                .load(R.drawable.ic_list_white).placeholder(R.drawable.ic_list_white)
                .into(ivAllList);
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
            case R.id.ivInAllListContainer:
                startIdDB = 0;
                boxListAdapter.clearList();
                filterType = BoxAddRfidViewModel.TYPE_FILTER_LIST_ALL;
                mViewModel.getBoxList(startIdDB, MIN_LIST_COUNT, filterType);
                ivInAllListContainer.setCardBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimary));
                tvAllList.setTextAppearance(getActivity(), R.style.TextStyle_Standart_Bold_White);
                tvAllListName.setTextAppearance(getActivity(), R.style.TextStyle_Small_Bold_White);
                ivWithRfidContainer.setCardBackgroundColor(getActivity().getResources().getColor(android.R.color.white));
                tvWithRfid.setTextAppearance(getActivity(), R.style.TextStyle_Standart_Bold_Accent);
                tvWithRfidName.setTextAppearance(getActivity(), R.style.TextStyle_Small_Bold_Grey);
                ivWithoutRfidContainer.setCardBackgroundColor(getActivity().getResources().getColor(android.R.color.white));
                tvWithoutRfid.setTextAppearance(getActivity(), R.style.TextStyle_Standart_Bold_Accent);
                tvWithoutRfidName.setTextAppearance(getActivity(), R.style.TextStyle_Small_Bold_Grey);
                Picasso.get()
                        .load(R.drawable.ic_list_white).placeholder(R.drawable.ic_list_white)
                        .into(ivAllList);
                Picasso.get()
                        .load(R.drawable.ic_check).placeholder(R.drawable.ic_check)
                        .into(ivWithRfid);
                Picasso.get()
                        .load(R.drawable.ic_close).placeholder(R.drawable.ic_close)
                        .into(ivWithoutRfid);
                break;
            case R.id.ivWithRfidContainer:
                startIdDB = 0;
                boxListAdapter.clearList();
                filterType = BoxAddRfidViewModel.TYPE_FILTER_LIST_WITH_RFID;
                mViewModel.getBoxList(startIdDB, MIN_LIST_COUNT, filterType);
                ivInAllListContainer.setCardBackgroundColor(getActivity().getResources().getColor(android.R.color.white));
                tvAllList.setTextAppearance(getActivity(), R.style.TextStyle_Standart_Bold_Accent);
                tvAllListName.setTextAppearance(getActivity(), R.style.TextStyle_Small_Bold_Grey);
                ivWithRfidContainer.setCardBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimary));
                tvWithRfid.setTextAppearance(getActivity(), R.style.TextStyle_Standart_Bold_White);
                tvWithRfidName.setTextAppearance(getActivity(), R.style.TextStyle_Small_Bold_White);
                ivWithoutRfidContainer.setCardBackgroundColor(getActivity().getResources().getColor(android.R.color.white));
                tvWithoutRfid.setTextAppearance(getActivity(), R.style.TextStyle_Standart_Bold_Accent);
                tvWithoutRfidName.setTextAppearance(getActivity(), R.style.TextStyle_Small_Bold_Grey);
                Picasso.get()
                        .load(R.drawable.ic_list).placeholder(R.drawable.ic_list)
                        .into(ivAllList);
                Picasso.get()
                        .load(R.drawable.ic_check_white).placeholder(R.drawable.ic_check_white)
                        .into(ivWithRfid);
                Picasso.get()
                        .load(R.drawable.ic_close).placeholder(R.drawable.ic_close)
                        .into(ivWithoutRfid);
                break;
            case R.id.ivWithoutRfidContainer:
                startIdDB = 0;
                boxListAdapter.clearList();
                filterType = BoxAddRfidViewModel.TYPE_FILTER_LIST_WITHOUT_RFID;
                mViewModel.getBoxList(startIdDB, MIN_LIST_COUNT, filterType);
                ivInAllListContainer.setCardBackgroundColor(getActivity().getResources().getColor(android.R.color.white));
                tvAllList.setTextAppearance(getActivity(), R.style.TextStyle_Standart_Bold_Accent);
                tvAllListName.setTextAppearance(getActivity(), R.style.TextStyle_Small_Bold_Grey);
                ivWithRfidContainer.setCardBackgroundColor(getActivity().getResources().getColor(android.R.color.white));
                tvWithRfid.setTextAppearance(getActivity(), R.style.TextStyle_Standart_Bold_Accent);
                tvWithRfidName.setTextAppearance(getActivity(), R.style.TextStyle_Small_Bold_Grey);
                ivWithoutRfidContainer.setCardBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimary));
                tvWithoutRfid.setTextAppearance(getActivity(), R.style.TextStyle_Standart_Bold_White);
                tvWithoutRfidName.setTextAppearance(getActivity(), R.style.TextStyle_Small_Bold_White);
                Picasso.get()
                        .load(R.drawable.ic_list).placeholder(R.drawable.ic_list)
                        .into(ivAllList);
                Picasso.get()
                        .load(R.drawable.ic_check).placeholder(R.drawable.ic_check)
                        .into(ivWithRfid);
                Picasso.get()
                        .load(R.drawable.ic_close_white).placeholder(R.drawable.ic_close_white)
                        .into(ivWithoutRfid);
                break;
            case R.id.btnStartStopRfid:
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
        if(foundedBox!=null) {
            if(TextUtils.isEmpty(scannedRfid) || !scannedRfid.equals(rfidCode)) {
                scannedRfid = rfidCode;
                getActivity().runOnUiThread(() -> {
                    ((MainActivity) getActivity()).setBlockRfidScan();
                    ((MainActivity) getActivity()).stopRfidScan();
                    onInventoryStopped();
                    mViewModel.getBoxByRfid(scannedRfid).observe(getViewLifecycleOwner(), result -> {
                        if (result == null && foundedBox != null) {
                            foundedBox.setRfid(scannedRfid);
                            if(!startSave) {
                                startSave = true;
                                mViewModel.saveBox(foundedBox);
                            }
                            rfidResultContainer.setVisibility(View.VISIBLE);
                            tvRfidResult.setText(scannedRfid);
                        }else{
                            if(result != null){
                                ActivityUtils.showShortToast(getActivity(),
                                        getString(R.string.mark_attached_to_box) + " " + result.getName());
                            }
                        }
                    });
                });
            }
        }
    }

    private void clearData(){
        foundedBox = null;
        setEnabledButton();
        fiacFindBox.setFilterList(new ArrayList<>());
        fiacFindBox.setText("");
        rfidResultContainer.setVisibility(View.INVISIBLE);
        tvRfidResult.setText("");
    }

    private void setEnabledButton(){

        if(foundedBox==null){
            btnStartStopRfid.setEnabled(false);
        }else{
            btnStartStopRfid.setEnabled(true);
        }
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
        if(foundedBox==null){
            return;
        }
        scannedRfid = "";
        startScan = true;
        btnStartStopRfid.setSelected(true);
        btnStartStopRfid.setText(getString(R.string.stop_rfid));
    }

    private void onInventoryStopped(){
        startScan = false;
        btnStartStopRfid.setSelected(false);
        btnStartStopRfid.setText(getString(R.string.start_rfid));
    }
}