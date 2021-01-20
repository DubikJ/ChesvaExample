package ua.com.expertsolution.chesva.model.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.expertsolution.chesva.model.dto.Box;
import ua.com.expertsolution.chesva.model.dto.Condition;
import ua.com.expertsolution.chesva.model.dto.MainAsset;
import ua.com.expertsolution.chesva.model.dto.Person;

public class LoadFile{
    @SerializedName("Boxes")
    private List<Box> boxes;
    @SerializedName("Conditions")
    private List<Condition> conditions;
    @SerializedName("MainAssets")
    private List<MainAsset> mainAssets;
    @SerializedName("Persons")
    private List<Person> persons;

    public LoadFile(List<Box> boxes, List<Condition> conditions, List<MainAsset> mainAssets, List<Person> persons) {
        this.boxes = boxes;
        this.conditions = conditions;
        this.mainAssets = mainAssets;
        this.persons = persons;
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public List<MainAsset> getMainAssets() {
        return mainAssets;
    }

    public List<Person> getPersons() {
        return persons;
    }
}
