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
import ua.com.expertsolution.chesva.model.dto.Operation;

public class OperationListAdapter extends RecyclerView.Adapter<OperationListAdapter.PersonViewHolder> {
    private final Context context;
    private List<Operation> list;
    private LayoutInflater inflater;
    private static AdapterListener adapterListener;
    private boolean forPerson;

    public OperationListAdapter(Context context, boolean forPerson, AdapterListener adapterListener) {
        this.context = context;
        this.list = new ArrayList<>();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.forPerson = forPerson;
        this.adapterListener = adapterListener;
    }

    public OperationListAdapter(Context context, List<Operation> list) {
        this.context = context;
        this.list = list;
    }

    public void setList(List<Operation> newlist){
        this.list = newlist;
        this.notifyDataSetChanged();
    }

    public List<Operation> getList() {
        return list;
    }

    public void addList(List<Operation> newlist){
        list.addAll(newlist);
        this.notifyDataSetChanged();
    }

    public void clearList(){
        list.clear();
        this.notifyDataSetChanged();
    }

    public void removeItem(int pos){
        try {
            list.remove(pos);
            this.notifyItemRemoved(pos);
        }catch (Exception e){}
    }

    public Operation getItem(int i) {
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

    public static class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        
        ImageView ivStatus;
        TextView tvStatus;
        TextView tvName;
        TextView tvComment;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvName = itemView.findViewById(R.id.tvName);
            tvComment = itemView.findViewById(R.id.tvComment);
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
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PersonViewHolder(inflater.inflate(R.layout.main_asset_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder viewHolder, final int i) {

        Operation item = getItem(i);
        if (item != null) {

            viewHolder.tvName.setText(item.getOwnerName());
            if(forPerson) {
                viewHolder.tvStatus.setText(item.getPersonName());
            }else{
                viewHolder.tvStatus.setText(item.getModelName());
            }
            viewHolder.tvComment.setText(item.getRfid());
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
