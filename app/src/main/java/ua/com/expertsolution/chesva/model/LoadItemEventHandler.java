package ua.com.expertsolution.chesva.model;

public class LoadItemEventHandler {
    public static final int LOAD_STARTED = 1;
    public static final int LOAD_ERROR = 2;
    public static final int LOAD_FINISH = 3;

    private int status;
    private String textError;
    private Object object;

    public LoadItemEventHandler(int status) {
        this.status = status;
    }

    public LoadItemEventHandler(int status, String textError) {
        this.status = status;
        this.textError = textError;
    }

    public LoadItemEventHandler(int status, Object object) {
        this.status = status;
        this.object = object;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTextError() {
        return textError;
    }

    public void setTextError(String textError) {
        this.textError = textError;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
