package com.edupath.qa.forum;

public enum QuestionCategory {
    ACADEMICS("Academics"),
    INTERNSHIPS("Internships"),
    HIGHER_STUDIES("Higher Studies"),
    PLACEMENT("Placement");

    private final String databaseValue;

    QuestionCategory(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String databaseValue() {
        return databaseValue;
    }
}
