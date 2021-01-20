package ua.com.expertsolution.chesva.model;

import java.util.ArrayList;
import java.util.List;

import ua.com.expertsolution.chesva.model.db.Facility;
import ua.com.expertsolution.chesva.model.dto.Box;
import ua.com.expertsolution.chesva.model.dto.MainAsset;
import ua.com.expertsolution.chesva.model.dto.Operation;

public class LoadListEventHandler {
    public static final int LOAD_STARTED = 1;
    public static final int LOAD_ERROR = 2;
    public static final int LOAD_FINISH = 3;

    private int status;
    private String textError;
    private List<Box> boxList;
    private List<MainAsset> mainAssets;
    private List<Operation> operations;

    public LoadListEventHandler(int status, String textError, List<Box> boxList, List<MainAsset> mainAssets,
                                List<Operation> operations) {
        this.status = status;
        this.textError = textError;
        this.boxList = boxList;
        this.mainAssets = mainAssets;
        this.operations = operations;
    }

    public int getStatus() {
        return status;
    }

    public String getTextError() {
        return textError;
    }

    public List<Box> getBoxList() {
        if(boxList==null){
            return new ArrayList<>();
        }
        return boxList;
    }

    public List<MainAsset> getMainAssets() {
        if(mainAssets==null){
            return new ArrayList<>();
        }
        return mainAssets;
    }

    public List<Operation> getOperations() {
        if(operations==null){
            return new ArrayList<>();
        }
        return operations;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int status;
        private String textError;
        private List<Box> boxList;
        private List<MainAsset> mainAssets;
        private List<Operation> operations;

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder found(String textError) {
            this.textError = textError;
            return this;
        }

        public Builder boxList(List<Box> boxList) {
            this.boxList = boxList;
            return this;
        }

        public Builder mainAssets(List<MainAsset> mainAssets) {
            this.mainAssets = mainAssets;
            return this;
        }

        public Builder operations(List<Operation> operations) {
            this.operations = operations;
            return this;
        }

        public LoadListEventHandler build() {
            return new LoadListEventHandler(status, textError, boxList, mainAssets, operations);
        }

    }
}
