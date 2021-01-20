package ua.com.expertsolution.chesva.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.model.OperatedLanguage;

public class LanguageAdapter extends ArrayAdapter<OperatedLanguage> {


    private Context context;
    private LayoutInflater inflater;
    private List<OperatedLanguage> list;
    private boolean isFullName;


    public LanguageAdapter(Context context, int resouceId, int textviewId, List<OperatedLanguage> list, boolean isFullName){
        super(context,resouceId,textviewId, list);
        this.list = list;
        this.context = context;
        this.isFullName = isFullName;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public OperatedLanguage getItem(int position) {
        try {
            return list.get(position);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return rowView(convertView,position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return rowView(convertView,position);
    }

    private View rowView(View convertView , int position){

        View rowView = convertView;
        OperatedLanguage item = getItem(position);
        ViewHolder holder;
        if (rowView == null) {

            holder = new ViewHolder();
            rowView = inflater.inflate(R.layout.item_language, null, false);

            holder.tvLanguage = (TextView) rowView.findViewById(R.id.tv_language);
            holder.ivLanguage = (ImageView) rowView.findViewById(R.id.iv_language);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        if(item != null) {
            holder.tvLanguage.setText(isFullName ? context.getString(item.getFullName()) : item.getName());
            holder.ivLanguage.setBackgroundResource(item.getIcon());
        }

        return rowView;
    }

    private class ViewHolder {
        TextView tvLanguage;
        ImageView ivLanguage;
    }
}
