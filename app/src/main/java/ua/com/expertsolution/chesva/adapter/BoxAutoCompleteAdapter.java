package ua.com.expertsolution.chesva.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import java.util.ArrayList;
import java.util.List;

import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.model.dto.Box;
import ua.com.expertsolution.chesva.model.dto.MainAsset;

import static ua.com.expertsolution.chesva.common.Consts.TAGLOG;

public class BoxAutoCompleteAdapter extends FilterAutoCompleteAdapter implements Filterable {
    public static final int SIZE_VIEW_LIST = 20;
    public static final int SIZE_FILTER_LIST = 300;


    private Context context;
    private LayoutInflater inflater;
    private List<Object> originalList;
    private List<Object> suggestions = new ArrayList<>();
    private Filter filter = new CustomFilter();
    private String textFilter = "";
    private int size = 0;


    public BoxAutoCompleteAdapter(Context context) {
        super(context);
        this.context = context;
        this.originalList = new ArrayList<>();
        this.inflater = LayoutInflater.from(context);
    }

    public void setOriginalList(List<?> originalList) {
        this.originalList = new ArrayList<>(originalList);
        getFilter().filter(textFilter);
    }

    public List<Object> getOriginalList() {
        return originalList;
    }

    public void clearList() {
        originalList.clear();
        clearSuggestionsList();
    }

    public void clearSuggestionsList() {
        suggestions.clear();
        this.notifyDataSetChanged();
    }

    public List<?> getSuggestions() {
        return suggestions;
    }

    @Override
    public void notifyDataSetChanged() {
        size = suggestions.size();
        try {
            super.notifyDataSetChanged();
        } catch (Exception e){
            Log.e(TAGLOG, e.toString());
        }
    }

    @Override
    public int getCount() {
        return size;//suggestions.size();
    }

    @Override
    public Object getItem(int position) {
        try {
            return ((Box)suggestions.get(position)).getName();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.select_dialog_singlechoice,
                    parent,
                    false);
        }

        TextView autoText = (TextView) convertView.findViewById(android.R.id.text1);
        try {
            autoText.setText(((String)getItem(position)));
        }catch (Exception e){
            Log.e(TAGLOG, e.toString());
        }

        return convertView;
    }

    public boolean isMoreOne(String value){
        return CollectionUtils.select(originalList, (Predicate) sample -> ((Box) sample).getName().toUpperCase()
                .contains(value.toUpperCase())).size()>1;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class CustomFilter extends Filter {
        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {

            suggestions.clear();
            FilterResults filterResults = new FilterResults();
            try {

                if (originalList != null && constraint != null) {
                    try {
                        suggestions.addAll(CollectionUtils.select(originalList, new Predicate() {
                            public boolean evaluate(Object sample) {
                                return ((Box) sample).getName().toUpperCase()
                                        .contains(constraint.toString().toUpperCase());
                            }
                        }));
                    } catch (Exception e) {
                        Log.e(TAGLOG, e.toString());
                    }
                } else {
                    try {
                        notifyDataSetChanged();
                    }catch (Exception e){
                        Log.e(TAGLOG, e.toString());
                    }
                }

                textFilter = constraint.toString();

            } catch (Exception e) {
                Log.e(TAGLOG, e.toString());
            }

            filterResults.values = suggestions.size() > SIZE_VIEW_LIST ? suggestions.subList(0, SIZE_VIEW_LIST): suggestions;
            filterResults.count = suggestions.size();

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0){
                try {
                    notifyDataSetChanged();
                }catch (Exception e){
                    Log.e(TAGLOG, e.toString());
                }
            }
        }
    }
}
