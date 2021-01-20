package ua.com.expertsolution.chesva.model.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.expertsolution.chesva.model.dto.Person;

public class PersonResponse extends DownloadResponse{

    @SerializedName("Total")
    private int total;
    @SerializedName("Persons")
    private List<Person> persons;

    public PersonResponse(String code, String message, int total, List<Person> persons) {
        super(code, message);
        this.total = total;
        this.persons = persons;
    }

    public int getTotal() {
        return total;
    }

    public List<Person> getPersons() {
        return persons;
    }
}
