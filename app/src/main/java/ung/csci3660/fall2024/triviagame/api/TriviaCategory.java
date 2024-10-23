package ung.csci3660.fall2024.triviagame.api;

// TODO: Add Documentation
public class TriviaCategory {

    private final int id;
    private final String name;

    public TriviaCategory(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
