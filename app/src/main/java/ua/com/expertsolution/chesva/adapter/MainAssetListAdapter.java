package ua.com.expertsolution.chesva.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.common.Consts;
import ua.com.expertsolution.chesva.model.dto.MainAsset;

public class MainAssetListAdapter extends RecyclerView.Adapter<MainAssetListAdapter.MainAssetViewHolder> {
    private final Context context;
    private List<MainAsset> list;
    private LayoutInflater inflater;
    private static AdapterListener adapterListener;
    private boolean isPerson;


    public MainAssetListAdapter(Context context, boolean isPerson, AdapterListener adapterListener) {
        this.context = context;
        this.list = new ArrayList<>();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.adapterListener = adapterListener;
        this.isPerson = isPerson;
    }

    public MainAssetListAdapter(Context context, List<MainAsset> list) {
        this.context = context;
        this.list = list;
    }

    public void setList(List<MainAsset> newlist){
        this.list = newlist;
        this.notifyDataSetChanged();
    }

    public List<MainAsset> getList() {
        return list;
    }

    public void addList(List<MainAsset> newlist){
        list.addAll(newlist);
        this.notifyDataSetChanged();
    }

    public void clearList(){
        list.clear();
        this.notifyDataSetChanged();
    }

    public MainAsset getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MainAssetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        
        ImageView ivStatus;
        TextView tvStatus;
        TextView tvName;
        TextView tvComment;
        TextView tvDate;

        public MainAssetViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvName = itemView.findViewById(R.id.tvName);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvDate = itemView.findViewById(R.id.tvDate);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            adapterListener.onItemClick(getAdapterPosition(), view);
        }

        @Override
        public boolean onLongClick(View view) {
            return adapterListener.onLongItemClick(getAdapterPosition(), view);
        }
    }

    @NonNull
    @Override
    public MainAssetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MainAssetViewHolder(inflater.inflate(R.layout.main_asset_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MainAssetViewHolder viewHolder, final int i) {

        MainAsset item = getItem(i);
        if (item != null) {

            viewHolder.tvName.setText(item.getName());
            viewHolder.tvStatus.setText(item.getModelName());
            viewHolder.tvComment.setText(item.getRfid());
            if(isPerson) {
                viewHolder.tvDate.setText(Consts.DATE_TIME_FORMAT.format(item.getTimeEditPerson()));
                viewHolder.tvDate.setVisibility(View.VISIBLE);
            }else{
                viewHolder.tvDate.setVisibility(View.GONE);
            }
            if(!TextUtils.isEmpty(item.getRfid())){
                Picasso.get()
                        .load(R.drawable.ic_check_white).placeholder(R.drawable.ic_check_white)
                        .into(viewHolder.ivStatus);
            }else{
                Picasso.get()
                        .load(R.drawable.ic_close_white).placeholder(R.drawable.ic_close_white)
                        .into(viewHolder.ivStatus);
            }
        }
    }

    public interface AdapterListener {
        void onItemClick(int position, View v);
        boolean onLongItemClick(int position, View v);
    }

}
