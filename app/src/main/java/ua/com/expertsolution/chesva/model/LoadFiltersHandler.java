package ua.com.expertsolution.chesva.model;

import java.util.List;

public class LoadFiltersHandler {

    private String column;
    private List<?> filterData;

    public LoadFiltersHandler(String column, List<?> filterData) {
        this.column = column;
        this.filterData = filterData;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public List<?> getFilterData() {
        return filterData;
    }

    public void setFilterData(List<?> filterData) {
        this.filterData = filterData;
    }
}
