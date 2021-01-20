package ua.com.expertsolution.chesva.ui.fragments.geiger;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.adapter.FacilityListAdapter;
import ua.com.expertsolution.chesva.model.LoadListEventHandler;
import ua.com.expertsolution.chesva.model.db.Facility;
import ua.com.expertsolution.chesva.scanner.DevBeep;
import ua.com.expertsolution.chesva.scanner.TagProximator;
import ua.com.expertsolution.chesva.ui.activities.MainActivity;
import ua.com.expertsolution.chesva.ui.fragments.FragmentInventoryCallback;
import ua.com.expertsolution.chesva.ui.fragments.FragmentInventoryListener;
import ua.com.expertsolution.chesva.ui.fragments.FragmentOnBackPressed;
import ua.com.expertsolution.chesva.ui.widgets.ShowHideRecyclerScrollListener;
import ua.com.expertsolution.chesva.utils.ActivityUtils;
import ua.com.expertsolution.chesva.utils.DeviceUtils;

public class GeigerFragment extends Fragment implements View.OnClickListener, FragmentOnBackPressed,
        FragmentInventoryListener, FragmentInventoryCallback {
    private final static int MIN_LIST_COUNT = 50;
    private static final long TAG_TIME_OUT = 1000;

    @BindView(R.id.llLabelSearch)
    LinearLayout llLabelSearch;

    @BindView(R.id.etLabelSearchLabelName)
    EditText etLabelSearchLabelName;

    @BindView(R.id.etLabelSearchObjName)
    EditText etLabelSearchObjName;

    @BindView(R.id.lvLabelSearchMain)
    RecyclerView lvLabelSearchMain;

    @BindView(R.id.btnLabelSearchStart)
    Button btnLabelSearchStart;

    @BindView(R.id.progress_bar_load)
    ProgressBar loadProgressBar;

    @BindView(R.id.geiger_progressBar)
    ProgressBar geigerProgressBar;

    @BindView(R.id.geiger_search_btn)
    Button geigerSearchBtn;

    @BindView(R.id.geiger_selected_container)
    LinearLayout geigerSelectedContainer;

    @BindView(R.id.geiger_rssi)
    TextView geigerRssi;

    @BindView(R.id.itemName)
    TextView itemName;

    @BindView(R.id.itemCode)
    TextView itemCode;

//    @BindView(R.id.nestedScrollView)
//    NestedScrollView nestedScrollView;

    private View view;
    private GeigerViewModel mViewModel;
    private FacilityListAdapter facilityListAdapter;
    private Facility selectedFacility;
    private Boolean startLoad = false;
    private Boolean startScan = false;
    private int startIdDB = 0;
    private long tagLastSeen = 0;
    public boolean closing = false;
    public boolean blockPagination = false;
    public static GeigerFragment newInstance() {
        return new GeigerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_geiger, container, false);
        ButterKnife.bind(this, view);

        mViewModel = new ViewModelProvider(this).get(GeigerViewModel.class);

        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).changeToolbar(getActivity().getString(R.string.geiger_txt_rssi),
                false, true, true);
        ((MainActivity)getActivity()).setFragmentRfidCallBack(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel.getResultLoad().observe(getViewLifecycleOwner(), newResult -> {
            switch (newResult.getStatus()) {
                case LoadListEventHandler.LOAD_STARTED:
                    loadProgressBar.setVisibility(View.VISIBLE);
                    startLoad = true;
                    break;
                case LoadListEventHandler.LOAD_ERROR:
                    loadProgressBar.setVisibility(View.GONE);
                    startLoad = false;
                    break;
                case LoadListEventHandler.LOAD_FINISH:
//                    if(newResult.getFacilityList().size()<MIN_LIST_COUNT){
//                        blockPagination = true;
//                    }else{
//                        blockPagination = false;
//                    }
//                    facilityListAdapter.addList(newResult.getFacilityList().size()>MIN_LIST_COUNT+1
//                            ? newResult.getFacilityList().subList(0, MIN_LIST_COUNT): newResult.getFacilityList());
//                    loadProgressBar.setVisibility(View.GONE);
//                    startLoad = false;
                    break;
            }
        });

        btnLabelSearchStart.setOnClickListener(this);
        btnLabelSearchStart.setEnabled(false);
        geigerSearchBtn.setOnClickListener(this);
        geigerSearchBtn.setEnabled(false);

        DevBeep.init(getContext());
        startSoundFeedbackThread();



        facilityListAdapter = new FacilityListAdapter(getActivity(), new FacilityListAdapter.AdapterListener() {
            @Override
            public void onItemClick(int position, View v) {
                if(startScan){
                    Toast.makeText(getActivity(),
                            getActivity().getString(R.string.wait_end_procces_inventory),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Facility facility = facilityListAdapter.getList().get(position);
                    if(facility!=null && selectedFacility!=null && facility.getId()==selectedFacility.getId()){
                        selectedFacility = null;
                        geigerSelectedContainer.setVisibility(View.GONE);
                        geigerSearchBtn.setEnabled(false);
                        ((MainActivity) getActivity()).setMaskScan(null);
                    }else {
                        selectedFacility = facility;
                        itemCode.setText(facility.getRfid());
                        itemName.setText(facility.getName());
                        geigerSelectedContainer.setVisibility(View.VISIBLE);
                        geigerSearchBtn.setEnabled(true);
                        String rfid = selectedFacility.getRfid();//"E200001C110302360500D5EA";
                        //   Mask mask = new Mask(Bank.EPC, 32, labelNum.length() * 4, labelNum);//Mask.maskEPC(labelNum);//new Mask(Bank.EPC, labelNum.length(), labelNum.length() * 4, labelNum)
                        ((MainActivity) getActivity()).setMaskScan(rfid);
                    }
                }catch (Exception e){}
            }

            @Override
            public boolean onLongItemClick(int position, View v) { return true; }

        });
        lvLabelSearchMain.setLayoutManager(new LinearLayoutManager(getActivity()));
        lvLabelSearchMain.setAdapter(facilityListAdapter);
//        lvLabelSearchMain.setNestedScrollingEnabled(false);

        lvLabelSearchMain.addOnScrollListener(new ShowHideRecyclerScrollListener() {
            @Override
            public void show() {
//                llLabelSearch.setVisibility(View.VISIBLE);
//                llLabelSearch.animate().translationY(0).setInterpolator
//                        (new DecelerateInterpolator(2)).start();
            }

            @Override
            public void hide() {
//                llLabelSearch.animate().translationY(-llLabelSearch.getHeight())
//                        .setInterpolator(new AccelerateInterpolator(2));
//                llLabelSearch.setVisibility(View.GONE);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();

                int totalItemCount = lm.getItemCount();
                int lastVisibleItem = lm
                        .findLastVisibleItemPosition();
                if (totalItemCount <= (lastVisibleItem + 1) && !startLoad && !blockPagination) {
                    loadProgressBar.setVisibility(View.VISIBLE);
                    try {
//                        int itemMax = totalItemCount - 1 + MIN_LIST_COUNT;
//                        List<Facility> facilityList = facilityListAdapter.getList();
//                        if (itemMax > facilityList.size()) {
//                            itemMax = facilityList.size();
//                        }
//                        List<Facility> subListFields = facilityList.subList(totalItemCount, itemMax);
//                        if (subListFields.size() > 0) {
//                            facilityListAdapter.addList(subListFields);
//                        } else {
//                            if (facilityList.size() >= MIN_LIST_COUNT  && !startload) {
//                                try {
//                                    startIdDB = facilityListAdapter.getList().get(facilityListAdapter.getList().size() - 1).getId();
//                                } catch (Exception e) {
//                                    startIdDB = 0;
//                                }
//                                mViewModel.getFacilityList(startIdDB, MIN_LIST_COUNT,
//                                        etLabelSearchObjName.getText().toString(), etLabelSearchLabelName.getText().toString());
//                            }
//                        }
                        try {
                            startIdDB = facilityListAdapter.getList().get(facilityListAdapter.getList().size() - 1).getId()+1;
                        } catch (Exception e) {
                            startIdDB = 0;
                        }
                        mViewModel.getFacilityList(startIdDB, MIN_LIST_COUNT,
                                etLabelSearchObjName.getText().toString(), etLabelSearchLabelName.getText().toString());
                    } catch (Exception e) {

                    }
                    if (!startLoad) {
                        loadProgressBar.setVisibility(View.GONE);
                    }
                }
            }
        });

//        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
//            if(v.getChildAt(v.getChildCount() - 1) != null) {
//                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
//                        scrollY > oldScrollY) {
//                    try {
//                        startIdDB = facilityListAdapter.getList().get(facilityListAdapter.getList().size() - 1).getId();
//                    } catch (Exception e) {
//                        startIdDB = 0;
//                    }
//                    mViewModel.getFacilityList(startIdDB, MIN_LIST_COUNT,
//                            etLabelSearchObjName.getText().toString(), etLabelSearchLabelName.getText().toString());
//                }
//            }
//        });

        etLabelSearchLabelName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                setEnabledButton();
            }
        });

        etLabelSearchObjName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                setEnabledButton();
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
            case R.id.btnLabelSearchStart:
                if(startScan){
                    Toast.makeText(getActivity(),
                            getActivity().getString(R.string.wait_end_procces_inventory),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                facilityListAdapter.getList().clear();
                facilityListAdapter.clearList();
                startIdDB = 0;
                mViewModel.getFacilityList(startIdDB, MIN_LIST_COUNT,
                        etLabelSearchObjName.getText().toString(), etLabelSearchLabelName.getText().toString());
                break;
            case R.id.geiger_search_btn:
                if(startScan){
                    ((MainActivity)getActivity()).stopRfidScan();
                    geigerSearchBtn.setText(getString(R.string.start));
                    geigerRssi.setText("0");
                    geigerProgressBar.setProgress(0);
                    startScan = false;
                }else{
                    ((MainActivity) getActivity()).startRfidScan();
                    geigerSearchBtn.setText(getString(R.string.stop));
                    startScan = true;
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
    public void onTagRead(String epc, String rssi) {
        DevBeep.PlayOK();
        tagLastSeen = System.currentTimeMillis();
        String RFIDtag = epc;
        double rssiDouble = 0;
        try{
            Double.parseDouble(rssi);
        }catch (Exception e){

        }
        TagProximator.addData(RFIDtag, rssiDouble);
        double normalizeRssi = (double) TagProximator.getProximity(RFIDtag);
        int range = TagProximator.getScaledProximity(RFIDtag, DeviceUtils.getDeviceName());
        final String tagRssi = String.format("%1$,.1f", new Object[]{Double.valueOf(normalizeRssi)});
        range = Math.min(range, 100);
        range = Math.max(range, 0);
        final int finalRange = range;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                geigerRssi.setText(tagRssi);
                geigerProgressBar.setProgress(finalRange);
            }
        });
    }

    private void startSoundFeedbackThread() {
        new Thread(new Runnable() {
            public void run() {
                while (!closing) {
                    refreshDetection();
                }
            }
        }).start();
    }

    private void setEnabledButton(){

        if((etLabelSearchLabelName.getText().length()==0 &&
                etLabelSearchObjName.getText().length()==0) ||
                (etLabelSearchLabelName.getText().length()>0 &&
                        etLabelSearchObjName.getText().length()>0)){
            btnLabelSearchStart.setEnabled(false);
        }else{
            btnLabelSearchStart.setEnabled(true);
        }
    }

    public void refreshDetection() {
        if (System.currentTimeMillis() - tagLastSeen > TAG_TIME_OUT && !geigerRssi.getText().equals("0")) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    geigerRssi.setText("0");
                    geigerProgressBar.setProgress(0);
                }
            });
        }
    }

    @Override
    public void startInventory() {
        if(!startScan) {
            geigerSearchBtn.setText(getString(R.string.stop));
            startScan = true;
        }
    }

    @Override
    public void stopInventory() {
        if(startScan) {
            geigerSearchBtn.setText(getString(R.string.start));
            geigerRssi.setText("0");
            geigerProgressBar.setProgress(0);
            startScan = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.closing = true;
        ((MainActivity) getActivity()).setMaskScan(null);
    }
}