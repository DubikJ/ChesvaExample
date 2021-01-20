package ua.com.expertsolution.chesva.adapter;

import android.content.Context;
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
import ua.com.expertsolution.chesva.model.db.Facility;

public class FacilityListAdapter extends RecyclerView.Adapter<FacilityListAdapter.FacilityViewHolder> {
    private final Context context;
    private List<Facility> list;
    private LayoutInflater inflater;
    private static AdapterListener adapterListener;

    public FacilityListAdapter(Context context, AdapterListener adapterListener) {
        this.context = context;
        this.list = new ArrayList<>();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.adapterListener = adapterListener;
    }

    public FacilityListAdapter(Context context, List<Facility> list) {
        this.context = context;
        this.list = list;
    }

    public void setList(List<Facility> newlist){
        this.list = newlist;
        this.notifyDataSetChanged();
    }

    public List<Facility> getList() {
        return list;
    }

    public void addList(List<Facility> newlist){
        list.addAll(newlist);
        this.notifyDataSetChanged();
    }

    public void clearList(){
        list.clear();
        this.notifyDataSetChanged();
    }

    public Facility getItem(int i) {
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

    public static class FacilityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        
        ImageView icon;
        TextView name;
        TextView rfid;

        public FacilityViewHolder(@NonNull View itemView) {
            super(itemView);
//            icon = itemView.findViewById(R.id.icon);
//            name = itemView.findViewById(R.id.name);
//            rfid = itemView.findViewById(R.id.rfid);
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
    public FacilityViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new FacilityViewHolder(inflater.inflate(R.layout.main_asset_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FacilityViewHolder viewHolder, final int i) {

        Facility item = getItem(i);
        if (item != null) {

            viewHolder.name.setText(item.getName()+" ("+item.getInventoryNumber()+")");
            viewHolder.rfid.setText(item.getRfid());
            switch (item.getState()) {
                case Consts.STATE_IN_SEARCH_OF:
                    Picasso.get()
                            .load(R.drawable.ic_search)
                            .error(R.drawable.ic_priority)
                            .into(viewHolder.icon);
                    break;
                case Consts.STATE_FOUND:
                    Picasso.get()
                            .load(R.drawable.ic_check)
                            .error(R.drawable.ic_priority)
                            .into(viewHolder.icon);
                    break;
                case Consts.STATE_EXCESSIVE:
                    Picasso.get()
                            .load(R.drawable.ic_clear)
                            .error(R.drawable.ic_priority)
                            .into(viewHolder.icon);
                    break;
                case Consts.STATE_CONFIRMED:
                    Picasso.get()
                            .load(R.drawable.ic_add)
                            .error(R.drawable.ic_priority)
                            .into(viewHolder.icon);
                    break;
                case Consts.STATE_MISSING:
                    Picasso.get()
                            .load(R.drawable.ic_priority)
                            .error(R.drawable.ic_priority)
                            .into(viewHolder.icon);
                    break;
                case Consts.STATE_CREATED:
                    Picasso.get()
                            .load(R.drawable.ic_created)
                            .error(R.drawable.ic_priority)
                            .into(viewHolder.icon);
                    break;
            }
        }
    }

    public interface AdapterListener {
        void onItemClick(int position, View v);
        boolean onLongItemClick(int position, View v);
    }

}
