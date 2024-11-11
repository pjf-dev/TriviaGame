package ung.csci3660.fall2024.triviagame.api;

import android.os.Parcel;
import android.os.Parcelable;

// Fairly self-explanatory
// Record class type added in Java 14
public record TriviaQuestion(ung.csci3660.fall2024.triviagame.api.TriviaQuestion.Type type,
                             ung.csci3660.fall2024.triviagame.api.TriviaQuestion.Difficulty difficulty, String question,
                             String categoryString, String correctAnswer, String[] incorrectAnswers) implements Parcelable {

    public enum Type {
        BOOLEAN,
        MULTIPLE,
        ANY // Used for query only, never for a question
    }

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD,
        ANY // Used for query only, never for a question
    }

    protected TriviaQuestion(Parcel in) {
        this(TriviaQuestion.Type.valueOf(in.readString()),
                Difficulty.valueOf(in.readString()),
                in.readString(),
                in.readString(),
                in.readString(),
                in.createStringArray());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type.toString());
        dest.writeString(difficulty.toString());
        dest.writeString(question);
        dest.writeString(categoryString);
        dest.writeString(correctAnswer);
        dest.writeStringArray(incorrectAnswers);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TriviaQuestion> CREATOR = new Creator<>() {
        @Override
        public TriviaQuestion createFromParcel(Parcel in) {
            return new TriviaQuestion(in);
        }

        @Override
        public TriviaQuestion[] newArray(int size) {
            return new TriviaQuestion[size];
        }
    };
}
