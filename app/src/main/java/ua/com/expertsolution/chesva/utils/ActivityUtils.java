package ua.com.expertsolution.chesva.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.PowerManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import ua.com.expertsolution.chesva.R;

import static android.content.Context.POWER_SERVICE;
import static ua.com.expertsolution.chesva.common.Consts.CHECK_PASSWORD;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG;

public class ActivityUtils {
    public static final int INPUT_TYPE_STRING = 0;
    public static final int INPUT_TYPE_INTEGER = 1;
    public static final int INPUT_TYPE_DOUBLE = 2;
    public static final int INPUT_TYPE_CODE = 3;
    public static final int INPUT_TYPE_DOUBLE_ONLY_PLUS = 4;

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    public static float getToolBarHeight(Context context) {
        int[] attrs = new int[] {R.attr.actionBarSize};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        float toolBarHeight = ta.getDimension(0, -1);
        ta.recycle();
        return toolBarHeight;
    }

    public static Boolean isActivityRunning(Context mContext, Class activityClass) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                return true;
        }

        return false;
    }

    public static Boolean checkDisplayIsOn(Context mContext){
        PowerManager powerManager = (PowerManager) mContext.getSystemService(POWER_SERVICE);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH &&
                    powerManager.isInteractive() ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH &&
                        powerManager.isScreenOn();
    }

    public static AlertDialog showMessage(Context mContext, String textTitle, Drawable drawableIconTitle,
                                          String textMessage) {
        if(mContext == null) return null;

        if (textMessage == null || textMessage.isEmpty()) {
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View titleView = layoutInflater.inflate(R.layout.dialog_title, null);
        ImageView imageTitle = titleView.findViewById(R.id.image_title);
        if(drawableIconTitle!=null){
            imageTitle.setVisibility(View.VISIBLE);
            imageTitle.setImageDrawable(drawableIconTitle);
        }
        TextView titleTV = titleView.findViewById(R.id.text_title);
        titleTV.setText(TextUtils.isEmpty(textTitle) ?
                mContext.getString(R.string.questions_title_error) :
                textTitle);
        builder.setCustomTitle(titleView);
        builder.setMessage(textMessage);

        builder.setNeutralButton(mContext.getString(R.string.questions_answer_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        try {
            dialog.show();
        }catch (Exception e){
            Log.e(TAGLOG, e.toString());
        }

        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        Button button1 = (Button) dialog.findViewById(android.R.id.button1);
        button1.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        Button button2 = (Button) dialog.findViewById(android.R.id.button2);
        button2.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        Button button3 = (Button) dialog.findViewById(android.R.id.button3);
        button3.setTextColor(mContext.getResources().getColor(R.color.colorAccent));

        return dialog;
    }

    public static AlertDialog showMessageWihtCallBack(Context mContext, String textTitle, Drawable drawableIconTitle,
                                               String textMessage, String buttonName, MessageCallBack messageCallBack) {
        if(mContext == null) return null;

        if (textMessage == null || textMessage.isEmpty()) {
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View titleView = layoutInflater.inflate(R.layout.dialog_title, null);
        ImageView imageTitle = titleView.findViewById(R.id.image_title);
        if(drawableIconTitle!=null){
            imageTitle.setVisibility(View.VISIBLE);
            imageTitle.setImageDrawable(drawableIconTitle);
        }
        TextView titleTV = titleView.findViewById(R.id.text_title);
        titleTV.setText(TextUtils.isEmpty(textTitle) ?
                mContext.getString(R.string.questions_title_error) :
                textTitle);
        builder.setCustomTitle(titleView);
        builder.setMessage(textMessage);

        builder.setNeutralButton(
                TextUtils.isEmpty(buttonName) ?
                        mContext.getString(R.string.questions_answer_ok) : buttonName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                messageCallBack.onPressOk();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        try {
            dialog.show();

            TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            textView.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            Button button1 = (Button) dialog.findViewById(android.R.id.button1);
            button1.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            Button button2 = (Button) dialog.findViewById(android.R.id.button2);
            button2.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            Button button3 = (Button) dialog.findViewById(android.R.id.button3);
            button3.setTextColor(mContext.getResources().getColor(R.color.colorAccent));

            return dialog;
        }catch (Exception e){
            Log.e(TAGLOG, e.toString());
        }

        return null;
    }

    public static void showShortToast(Context mContext, String message){
        if(mContext == null) return;
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context mContext, String message){
        if(mContext == null) return;
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    public static void showQuestion(Context mContext, String textTitle, Drawable drawableIconTitle,
                                    String textMessage,
                                    String nameButton1, String nameButton2, String nameButton3,
                                    final QuestionAnswer questionAnswer) {
        if(mContext == null) return;
        if (textMessage == null || textMessage.isEmpty()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View titleView = layoutInflater.inflate(R.layout.dialog_title, null);
        ImageView imageTitle = titleView.findViewById(R.id.image_title);
        if(drawableIconTitle==null){
            imageTitle.setVisibility(View.GONE);
        }else {
            imageTitle.setVisibility(View.VISIBLE);
            imageTitle.setImageDrawable(drawableIconTitle);
        }
        TextView titleTV = titleView.findViewById(R.id.text_title);
        titleTV.setText(textTitle != null && !textTitle.isEmpty() ? textTitle :
                mContext.getString(R.string.questions_title_question));

        builder.setCustomTitle(titleView);
        builder.setMessage(textMessage);

        builder.setPositiveButton(TextUtils.isEmpty(nameButton1) ?
                mContext.getString(R.string.questions_answer_yes) : nameButton1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                questionAnswer.onPositiveAnswer();
            }
        });

        builder.setNegativeButton(TextUtils.isEmpty(nameButton2) ?
                mContext.getString(R.string.questions_answer_no) : nameButton2, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                questionAnswer.onNegativeAnswer();
            }
        });

        if(!TextUtils.isEmpty(nameButton3)) {
            builder.setNeutralButton(nameButton3,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            questionAnswer.onNeutralAnswer();
                        }
                    });
        }

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                questionAnswer.onNegativeAnswer();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        try {
            dialog.show();
        }catch (Exception e){
            Log.e(TAGLOG, e.toString());
        }

        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        Button button1 = (Button) dialog.findViewById(android.R.id.button1);
        button1.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        Button button2 = (Button) dialog.findViewById(android.R.id.button2);
        button2.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        Button button3 = (Button) dialog.findViewById(android.R.id.button3);
        button3.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
    }

    public static Snackbar showSnackBar(Context mContext, View viewParent, int colorBackground,
                                        String textMessage, SnackBarCallBack snackBarCallBack){
        View custom = LayoutInflater.from(mContext).inflate(R.layout.layout_new_version, null);
        Snackbar snackbar = Snackbar.make(viewParent, "", Snackbar.LENGTH_INDEFINITE);
        snackbar.getView().setPadding(0,0,0,0);
        ((ViewGroup) snackbar.getView()).removeAllViews();
        ((ViewGroup) snackbar.getView()).addView(custom);
        if(colorBackground>0) {
            RelativeLayout container = custom.findViewById(R.id.container);
            container.setBackgroundResource(colorBackground);
        }
        TextView updateNow = custom.findViewById(R.id.update_now);
        updateNow.setText(textMessage);
        updateNow.setOnClickListener(v -> {
            snackBarCallBack.onCallBack();
        });
        TextView updateClose = custom.findViewById(R.id.close_update);
        updateClose.setOnClickListener(view -> {
            snackbar.dismiss();
        });
        try {
            snackbar.show();
        }catch (Exception e){
            Log.e(TAGLOG, e.toString());
        }

        return snackbar;
    }

    public static void hideKeyboard(Context context){
        if(context == null) return;
        ((InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    public static void showKeyboard(Context context, View view){
        if(context == null) return;
        ((InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE))
                .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void showSelectionList(Context mContext, String textTitle, Drawable drawableIconTitle,
                                         final List<String> listString, final ListItemClick listItemClick) {
        if(mContext == null) return;
        if (listString == null) {
            return;
        }

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

        builder.setAdapter(new ArrayAdapter<String>(mContext,
                        R.layout.row_sevice_item, R.id.textItem, listString),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listItemClick.onItemClik(which, listString.get(which));
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        try {
            dialog.show();
        }catch (Exception e){
            Log.e(TAGLOG, e.toString());
        }
    }

    public static void showSelectionListByAdapter(Context mContext, String textTitle, Drawable drawableIconTitle,
                                                  ListAdapter listAdapter, final ListItemClick listItemClick) {
        if(mContext == null) return;
        if (listAdapter == null) {
            return;
        }

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

        builder.setAdapter(listAdapter, (DialogInterface.OnClickListener) (dialog, which) ->
                listItemClick.onItemClik(which, null));

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        try {
            dialog.show();
        }catch (Exception e){
            Log.e(TAGLOG, e.toString());
        }
    }

    public interface ListItemClick {

        void onItemClik(int item, Object value);
    }

    public static void showDatePicket(Context context, int year, int monthOfYear, int dayOfMonth,
                                      final DatePicketSet datePicketSet){
        if(context == null) return;

        DatePickerDialog.OnDateSetListener dateDialog = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                datePicketSet.onDateSet(year, monthOfYear, dayOfMonth);
            }
        };

        try {
            new DatePickerDialog(context, dateDialog, year, monthOfYear, dayOfMonth).show();;
        }catch (Exception e){
            Log.e(TAGLOG, e.toString());
        }
    }

    public interface DatePicketSet {

        void onDateSet(int year, int monthOfYear, int dayOfMonth);
    }

    public static void showTimePicket(Context context, int hourOfDay, int minute, boolean is24HourView,
                                      final TimePicketSet timePicketSet) {

        final int[] noOfTimesCalled = {0};

        TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (view.isShown()) {
                    if (noOfTimesCalled[0] % 2 == 0) {
                        timePicketSet.onTimeSet(hourOfDay, minute);
                    }
                    noOfTimesCalled[0]++;

                }
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                myTimeListener, hourOfDay, minute, is24HourView);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View titleView = layoutInflater.inflate(R.layout.dialog_title, null);
        ImageView imageTitle = titleView.findViewById(R.id.image_title);
        imageTitle.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_time_white));
        TextView titleTV = titleView.findViewById(R.id.text_title);
        titleTV.setText(context.getString(R.string.questions_choose_time));
        timePickerDialog.setCustomTitle(titleView);

        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();

    }

    public interface TimePicketSet {

        void onTimeSet(int hourOfDay, int minute);
    }

    public static int getHeightDisplay(Context context){
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display. getSize(size);
        return size. y;
    }

    public static int getWidthDisplay(Context context){
        Display display = ((Activity)context).getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display. getSize(size);
        return size. x;
    }

    public static AlertDialog showInputDialog(Context mContext, int typeInput, String textTitle,
                                              Drawable drawableIconTitle, String previousData,
                                              String hint, String nameButton1, String nameButton2,
                                              Drawable drawableButton1, Drawable drawableButton2,
                                              InputDialogCallBackListener callBackListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        View titleView = LayoutInflater.from(mContext).inflate(R.layout.dialog_title, null);
        RelativeLayout containerTitle = titleView.findViewById(R.id.rlsubhead);
        containerTitle.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
        ImageView imageTitle = titleView.findViewById(R.id.image_title);
        if(drawableIconTitle==null){
            imageTitle.setVisibility(View.GONE);
        }else {
            imageTitle.setVisibility(View.VISIBLE);
            imageTitle.setImageDrawable(drawableIconTitle);
        }
        TextView titleTV = titleView.findViewById(R.id.text_title);
        titleTV.setText(textTitle);
        titleTV.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        builder.setCustomTitle(titleView);

        View viewInflated = LayoutInflater.from(mContext).inflate(R.layout.layout_input, null);
        EditText inputData = viewInflated.findViewById(R.id.input_data);
        inputData.setHint(hint);
        inputData.setText(previousData==null? "" : previousData);
        inputData.setSelectAllOnFocus(true);

        ImageView imageLeft = viewInflated.findViewById(R.id.image_left);
        imageLeft.setOnClickListener((v)->{
            switch (typeInput) {
                case INPUT_TYPE_INTEGER:
                    inputData.setText(String.valueOf(
                            Integer.valueOf(
                                    TextUtils.isEmpty(inputData.getText().toString())
                                            ? "0" : inputData.getText().toString())+ 1));
                    break;
                case INPUT_TYPE_DOUBLE:
                case INPUT_TYPE_DOUBLE_ONLY_PLUS:
                    inputData.setText(String.valueOf(
                            Double.valueOf(
                                    TextUtils.isEmpty(inputData.getText().toString())
                                            ? "0.0" : inputData.getText().toString())
                                    + 1));
                    break;
            }
        });

        ImageView imageRight = viewInflated.findViewById(R.id.image_right);
        imageRight.setOnClickListener((v)->{
            switch (typeInput) {
                case INPUT_TYPE_INTEGER:
                    if(TextUtils.isEmpty(inputData.getText().toString())){
                        inputData.setText(String.valueOf("0"));
                        return;
                    }
                    try {
                        int resI = Integer.valueOf(inputData.getText().toString()) - 1;
                        if (resI < 0) {
                            resI = 0;
                        }
                        inputData.setText(String.valueOf(resI));
                    }catch (Exception e){
                        inputData.setText(String.valueOf("0"));
                    }
                    break;
                case INPUT_TYPE_DOUBLE:
                    if(TextUtils.isEmpty(inputData.getText().toString())){
                        inputData.setText(String.valueOf("0.0"));
                        return;
                    }
                    try {
                        Double resD = Double.valueOf(inputData.getText().toString())- 1;
                        if(resD<0){
                            resD = 0.0;
                        }
                        inputData.setText(String.valueOf(resD));
                    }catch (Exception e){
                        inputData.setText(String.valueOf("0.0"));
                    }
                    break;
                default:
                    inputData.setText("");
                    break;
            }

        });

        switch (typeInput){
            case INPUT_TYPE_INTEGER:
                imageLeft.setVisibility(View.VISIBLE);
                imageRight.setVisibility(View.VISIBLE);
                imageRight.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_remove));
                inputData.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case INPUT_TYPE_DOUBLE:
                imageLeft.setVisibility(View.VISIBLE);
                imageRight.setVisibility(View.VISIBLE);
                imageRight.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_remove));
                inputData.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case INPUT_TYPE_CODE:
                imageLeft.setVisibility(View.GONE);
                imageRight.setVisibility(View.GONE);
                inputData.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case INPUT_TYPE_DOUBLE_ONLY_PLUS:
                imageLeft.setVisibility(View.VISIBLE);
                imageRight.setVisibility(View.GONE);
                inputData.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            default:
                imageLeft.setVisibility(View.GONE);
                imageRight.setVisibility(View.VISIBLE);
                imageRight.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_clear));
                inputData.setInputType(InputType.TYPE_CLASS_TEXT);
                inputData.setEms(500);
                break;
        }

        builder.setView(viewInflated);

        AlertDialog alertDialog = builder.create();
        Button buttonCancel = viewInflated.findViewById(R.id.button_cancel);
        if(!TextUtils.isEmpty(nameButton1)){
            buttonCancel.setText(nameButton1);
        }
        buttonCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
            callBackListener.onNegativeAnswer();
        });
        if(drawableButton1!=null){
            buttonCancel.setCompoundDrawablesWithIntrinsicBounds( null, null, drawableButton1, null );
        }
        Button buttonSave = viewInflated.findViewById(R.id.button_save);
        if(!TextUtils.isEmpty(nameButton2)){
            buttonSave.setText(nameButton2);
        }
        buttonSave.setOnClickListener(v -> {
            alertDialog.cancel();
            callBackListener.onPositiveAnswer(inputData.getText().toString(), typeInput);

        });
        if(drawableButton2!=null){
            buttonSave.setCompoundDrawablesWithIntrinsicBounds( null, null, drawableButton2, null );
        }
        alertDialog.setOnCancelListener(dialog -> {
            alertDialog.dismiss();
            callBackListener.onNegativeAnswer();
        });

        inputData.setOnEditorActionListener((v, actionId, event)-> {
            boolean handled = false;
            if(TextUtils.isEmpty(inputData.getText().toString())){
                return false;
            }
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    actionId == EditorInfo.IME_ACTION_GO ||
                    actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                if(alertDialog !=null) {
                    alertDialog.dismiss();
                }
                callBackListener.onPositiveAnswer(inputData.getText().toString(), typeInput);
                handled = true;
            }
            return handled;
        });

        new android.os.Handler().postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(inputData, InputMethodManager.SHOW_IMPLICIT);
        }, 300);

        try {
            alertDialog.show();
        }catch (Exception e){
            Log.e(TAGLOG, e.toString());
        }
        return alertDialog;
    }

    public static ProgressDialog showDialog(Context mContext, String title, String message){
        ProgressDialog dialog = new ProgressDialog(mContext, R.style.WhiteDialogTheme);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if(!TextUtils.isEmpty(title)) {
            dialog.setTitle(title);
        }
        if(!TextUtils.isEmpty(message)) {
            dialog.setMessage(message);
        }
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static void showDialogPassword(Context mContext, OnPassEnterCallBack onPassEnterCallBack){

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.WhiteDialogTheme);

        View titleView = LayoutInflater.from(mContext).inflate(R.layout.dialog_title, null);

        TextView titleTV = titleView.findViewById(R.id.text_title);
        titleTV.setText(mContext.getResources().getString(R.string.enter_password));

        builder.setCustomTitle(titleView);

        final View viewInflated = LayoutInflater.from(mContext).inflate(R.layout.layout_password, null);

        TextInputLayout passwordTL = viewInflated.findViewById(R.id.password_layout);
        EditText passwordET = viewInflated.findViewById(R.id.password);
        builder.setView(viewInflated);

        builder.setPositiveButton(mContext.getResources().getString(R.string.questions_answer_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        builder.setNegativeButton(mContext.getResources().getString(R.string.questions_answer_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });

        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        AlertDialog dialogPass = builder.create();
        dialogPass.show();
        dialogPass.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialogPass.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        dialogPass.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v)-> {
            String text = passwordET.getText().toString();
            if (TextUtils.isEmpty(text) || !text.equalsIgnoreCase(CHECK_PASSWORD)) {
                passwordTL.setError(mContext.getString(R.string.error_password));
                return;
            }
            dialogPass.cancel();
            onPassEnterCallBack.onCallBack();
        });
        dialogPass.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        dialogPass.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener((v)-> {
            dialogPass.cancel();
        });
    }

    public static int getStatusBarHeight(Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    public interface MessageCallBack {

        void onPressOk();

    }

    public interface SnackBarCallBack {

        void onCallBack();

    }

    public static int getOrientationDisplay(Context context){
        return context.getResources().getConfiguration().orientation;
    }

    public interface QuestionAnswer {

        void onPositiveAnswer();

        void onNegativeAnswer();

        void onNeutralAnswer();

    }

    public interface InputDialogCallBackListener {

        void onPositiveAnswer(String result, int inputType);

        void onNegativeAnswer();

    }

    public interface OnPassEnterCallBack{
        void onCallBack();
    }


}
