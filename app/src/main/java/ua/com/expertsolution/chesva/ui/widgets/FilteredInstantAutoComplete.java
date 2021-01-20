package ua.com.expertsolution.chesva.ui.widgets;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import java.util.List;

import ua.com.expertsolution.chesva.R;
import ua.com.expertsolution.chesva.adapter.FilterAutoCompleteAdapter;

import static ua.com.expertsolution.chesva.common.Consts.TAGLOG;

@SuppressLint("AppCompatCustomView")
public class FilteredInstantAutoComplete extends FrameLayout {

    private TextInputLayout textInputLayout;
    private InstantAutoComplete instantAutoComplete;
    private ProgressBar progressBar;
    private FilterAutoCompleteAdapter filterAutoCompleteAdapter;
    private boolean lockLoad;


    public FilteredInstantAutoComplete(Context context) {
        super(context);
        initView(null);
    }

    public FilteredInstantAutoComplete(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        initView(arg1);
    }

    public FilteredInstantAutoComplete(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
        initView(arg1);
    }

    private void initView(AttributeSet attrs) {
        inflate(getContext(), R.layout.layout_filter_load_textview, this);
        this.textInputLayout = findViewById(R.id.textInputLayout);
        this.instantAutoComplete = findViewById(R.id.instantAutoComplete);
        this.progressBar = findViewById(R.id.progressBar);

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.FilteredInstantAutoComplete,
                    0, 0);
            try {
                textInputLayout.setHint(
                        a.getString(
                                R.styleable.FilteredInstantAutoComplete_hint));
                instantAutoComplete.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        a.getDrawable(R.styleable.FilteredInstantAutoComplete_drawableRight), null);
            } finally {
                a.recycle();
            }
        }
    }

    public void init(Context context, FilterAutoCompleteAdapter  adapter, CallBackListener callBackListener){
        lockLoad = false;
        filterAutoCompleteAdapter = adapter == null ? new FilterAutoCompleteAdapter(context) : adapter;
        instantAutoComplete.setAdapter(filterAutoCompleteAdapter);
        instantAutoComplete.addTextChangedListener(new TextWatcher() {
            private int textLenght;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                textLenght = charSequence.length();
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                if(callBackListener!=null){
                    callBackListener.afterTextChanged(editable);
                }
                if(textLenght != editable.length()){
                    if(editable.length()>=3) {
                        if(filterAutoCompleteAdapter.getOriginalList().size() > 0 &&
                                filterAutoCompleteAdapter.getOriginalList().size() < FilterAutoCompleteAdapter.SIZE_FILTER_LIST) {
                            filterAutoCompleteAdapter.getFilter().filter(editable);
                        }else {
                            if(!lockLoad) {
                                if (callBackListener != null) {
                                    callBackListener.startLoadData(editable);
                                }
                                progressBar.setVisibility(View.VISIBLE);
                            }
                        }
                    }else{
                        filterAutoCompleteAdapter.clearList();
                    }
                }
            }
        });

        instantAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            if(callBackListener!=null){
                Object o = null;
                boolean isMoreOne = false;
                try {
                    o = filterAutoCompleteAdapter.getOriginalList().get(position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    isMoreOne = CollectionUtils.select(filterAutoCompleteAdapter.getOriginalList(), (Predicate)
                            sample -> ((String) sample).toUpperCase()
                                    .equals(instantAutoComplete.getText().toString().toUpperCase())).size()>1;
//                    Log.d(TAGLOG, filterAutoCompleteAdapter.getOriginalList().toString());
//                    Log.d(TAGLOG, instantAutoComplete.getText().toString());
//                    Log.d(TAGLOG, CollectionUtils.select(filterAutoCompleteAdapter.getOriginalList(), (Predicate)
//                            sample -> ((String) sample).toUpperCase()
//                                    .equals(instantAutoComplete.getText().toString().toUpperCase())).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                callBackListener.onSelectItem(instantAutoComplete.getText(), o, isMoreOne ? instantAutoComplete.getText() : null);
            }
        });

        instantAutoComplete.setOnTouchListener(new View.OnTouchListener() {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int leftEdgeOfRightDrawable = v.getRight()
                            - ((EditText)v).getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    if (event.getRawX() >= leftEdgeOfRightDrawable) {
                        if(callBackListener!=null){
                            callBackListener.onPressRightButton();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        instantAutoComplete.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if(callBackListener!=null){
                    callBackListener.startLoadData(instantAutoComplete.getText());
                }
            }
            return handled;
        });

        instantAutoComplete.setOnFocusChangeListener((view, hasFocus) -> {
            this.lockLoad = false;
        });
    }

    public void setText(String text) {
        this.instantAutoComplete.setText(text);
    }

    public void setText(String text, boolean showDropBox) {
        this.instantAutoComplete.setText(text);
        if(showDropBox){
            this.instantAutoComplete.showDropDown();
        }else{
            this.instantAutoComplete.dismissDropDown();
        }
    }

    public Editable getText() {
        return this.instantAutoComplete.getText();
    }

    public void setFilterList(List<?> list){
        filterAutoCompleteAdapter.setOriginalList(list);
//        instantAutoComplete.showDropDown();
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void setFilterList(List<?> list, boolean showDropDown){
        filterAutoCompleteAdapter.setOriginalList(list);
        if(showDropDown) {
            instantAutoComplete.showDropDown();
        }else{
            instantAutoComplete.dismissDropDown();
        }
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void setEditable(boolean editable){
        instantAutoComplete.setEnabled(editable);
    }

    public void dismissDropDown() {
        this.instantAutoComplete.dismissDropDown();
    }

    public void clearFocusEditText() {
        this.instantAutoComplete.clearFocus();
    }

    public void setLockLoad(boolean lockLoad) {
        this.lockLoad = lockLoad;
    }

    public interface CallBackListener{
        void afterTextChanged(Editable editable);
        void startLoadData(Editable editable);
        void onSelectItem(Editable editable, Object o, Editable editableMoreOne);
        void onPressRightButton();
    }


}
