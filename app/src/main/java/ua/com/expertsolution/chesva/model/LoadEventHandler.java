package ua.com.expertsolution.chesva.model;

public class LoadEventHandler {
    public static final int LOAD_STARTED = 1;
    public static final int LOAD_DISMISS = 2;
    public static final int LOAD_PROGRESS = 3;
    public static final int LOAD_ERROR = 4;
    public static final int LOAD_OPEN_FILE = 5;
    public static final int LOAD_CLEAR_PREVIOUS_DATA = 6;
    public static final int LOAD_SAVING_TO_DB = 7;
    public static final int LOAD_FINISH = 8;

    public static final int UPLOAD_STARTED = 1;
    public static final int UPLOAD_DISMISS = 2;
    public static final int UPLOAD_PROGRESS = 3;
    public static final int UPLOAD_ERROR = 4;
    public static final int UPLOAD_SAVING_TO_FILE = 5;
    public static final int UPLOAD_FINISH = 6;

    private int status;
    private int progress;
    private int maxProgress;
    private String text;

    public LoadEventHandler(int status) {
        this.status = status;
    }

    public LoadEventHandler(int status, String text, int progress, int maxProgress) {
        this.status = status;
        this.text = text;
        this.progress = progress;
        this.maxProgress = maxProgress;
    }

    public LoadEventHandler(int status, String text) {
        this.status = status;
        this.text = text;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
