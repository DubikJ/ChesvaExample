package ua.com.expertsolution.chesva.model;

import ua.com.expertsolution.chesva.R;

public enum  OperatedLanguage {
    ENGLISH("EN", R.drawable.ic_united_kingdom, "en", R.string.english),
//    UKRAINIAN("UA", R.drawable.ic_ukraine, "uk", R.string.ukrainian),
    RUSSIAN("RU", R.drawable.ic_russia, "ru", R.string.russian);

    private String name;
    private int icon;
    private String code;
    private int fullName;

    OperatedLanguage(String name, int icon, String code, int fullName) {
        this.name = name;
        this.icon = icon;
        this.code = code;
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    public String getCode() {
        return code;
    }

    public int getFullName() {
        return fullName;
    }
}
