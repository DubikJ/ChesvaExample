package ua.com.expertsolution.chesva.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.adapter.MainAssetListAdapter;
import ua.com.expertsolution.chesva.model.dto.MainAsset;

import static ua.com.expertsolution.chesva.common.Consts.TAGLOG;

public class LoadListDialog {
    public final static int MIN_LIST_COUNT = 20;

    private Context mContext;
    private CallBackListener callBackListener;
    private AlertDialog dialog;
    private MainAssetListAdapter listAdapter;
    private ProgressBar loadProgressBar;
    private Boolean startLoad = false;
    private int startIdDB = 0;
    public boolean blockPagination = false;
    private List<MainAsset> mainAssetListAll;
    public LoadListDialog(Context mContext, CallBackListener callBackListener) {
        this.mContext = mContext;
        this.callBackListener = callBackListener;
        this.listAdapter = new MainAssetListAdapter(mContext, false, new MainAssetListAdapter.AdapterListener() {
            @Override
            public void onItemClick(int position, View v) {
                if(callBackListener!=null){
                    callBackListener.onPressItem(position, listAdapter.getItem(position));
                }
            }

            @Override
            public boolean onLongItemClick(int position, View v) { return true; }

        });
    }

    public void show(String textTitle, Drawable drawableIconTitle, String nameButton, List<MainAsset> mainAssetListAll){
        if(mContext == null || mainAssetListAll == null) return;
        this.mainAssetListAll = mainAssetListAll;

        List<MainAsset>list = getList(0);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(textTitle != null && !textTitle.isEmpty() ? textTitle : mContext.getString(R.string.questions_title_info));

        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View titleView = layoutInflater.inflate(R.layout.dialog_title, null);
        ImageView imageTitle = titleView.findViewById(R.id.image_title);
        if(drawableIconTitle==null){
            imageTitle.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_playlist_add_check_white));
        }else {
            imageTitle.setImageDrawable(drawableIconTitle);
        }
        TextView titleTV = titleView.findViewById(R.id.text_title);
        titleTV.setText(textTitle != null && !textTitle.isEmpty() ? textTitle :
                mContext.getString(R.string.questions_select_from_list));
        builder.setCustomTitle(titleView);

        View viewInflated = LayoutInflater.from(mContext).inflate(R.layout.layout_load_list, null);
        RecyclerView listRV = viewInflated.findViewById(R.id.list);
        loadProgressBar = viewInflated.findViewById(R.id.progress_bar_load);

        listRV.setLayoutManager(new LinearLayoutManager(mContext));
        listRV.setAdapter(listAdapter);
        listRV.setNestedScrollingEnabled(false);
        if(list==null){
            loadProgressBar.setVisibility(View.VISIBLE);
        }else{
            finishLoad(list);
        }

        listRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        try {
                            startIdDB = listAdapter.getList().get(listAdapter.getList().size() - 1).getId()+1;
                        } catch (Exception e) {
                            startIdDB = 0;
                        }

                        finishLoad(getList(startIdDB));

                        if(callBackListener!=null){
                            callBackListener.startUpdate(startIdDB, MIN_LIST_COUNT);
                        }
                    } catch (Exception e) {

                    }
                    if (!startLoad) {
                        loadProgressBar.setVisibility(View.GONE);
                    }
                }
            }
        });

        builder.setView(viewInflated);

        if(!TextUtils.isEmpty(nameButton)) {
            builder.setNeutralButton(nameButton,
                    (dialog, which) -> {
                        callBackListener.onPressButton();
                    });
        }
        builder.setOnCancelListener(dialogInterface -> {
            close();
            if(callBackListener!=null){
                callBackListener.onCancel();
            }
        });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        try {
            dialog.show();
        }catch (Exception e){
            Log.e(TAGLOG, e.toString());
        }
    }

    public void startLoad(){
        loadProgressBar.setVisibility(View.VISIBLE);
        startLoad = true;
    }

    public void errorLoad(){
        loadProgressBar.setVisibility(View.GONE);
        startLoad = false;
    }

    private void finishLoad(List<MainAsset> list){
        if(list==null){
            blockPagination = true;
            loadProgressBar.setVisibility(View.GONE);
            startLoad = false;
            return;
        }
        if(list.size()<MIN_LIST_COUNT){
            blockPagination = true;
        }else{
            blockPagination = false;
        }
        listAdapter.addList(list.size()>MIN_LIST_COUNT+1
                ? list.subList(0, MIN_LIST_COUNT): list);
        loadProgressBar.setVisibility(View.GONE);
        startLoad = false;
    }

    public void close(){
        if(dialog!=null){
            dialog.dismiss();
        }
    }

    private List<MainAsset> getList(int startId){
        if(mainAssetListAll.size()<MIN_LIST_COUNT){
            return mainAssetListAll;
        }else{
            return mainAssetListAll.subList(startId,
                    (startId+MIN_LIST_COUNT)>mainAssetListAll.size()? mainAssetListAll.size() :startId+MIN_LIST_COUNT);
        }
    }

    public interface CallBackListener{
        void onPressItem(int position, MainAsset mainAsset);

        void startUpdate(int startId, int count);

        void onPressButton();

        void onCancel();
    }
}
