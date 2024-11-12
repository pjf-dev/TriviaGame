package ung.csci3660.fall2024.triviagame.game;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks game progress and statistics
 */
public class GameResult implements Parcelable {
    private final List<Integer> scores;
    private final List<Integer> correctAnswers;
    private final List<Integer> totalAnswered;
    private final List<Integer> lastAnswerPoints;

    public GameResult(int numPlayers) {
        this.scores = new ArrayList<>(numPlayers);
        this.correctAnswers = new ArrayList<>(numPlayers);
        this.totalAnswered = new ArrayList<>(numPlayers);
        this.lastAnswerPoints = new ArrayList<>(numPlayers);

        for (int i = 0; i < numPlayers; i++) {
            scores.add(0);
            correctAnswers.add(0);
            totalAnswered.add(0);
            lastAnswerPoints.add(0);
        }
    }

    protected GameResult(Parcel in) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            scores = in.readArrayList(Integer.class.getClassLoader(), Integer.class);
            correctAnswers = in.readArrayList(Integer.class.getClassLoader(), Integer.class);
            totalAnswered = in.readArrayList(Integer.class.getClassLoader(), Integer.class);
            lastAnswerPoints = in.readArrayList(Integer.class.getClassLoader(), Integer.class);
        } else {
            scores = in.readArrayList(Integer.class.getClassLoader());
            correctAnswers = in.readArrayList(Integer.class.getClassLoader());
            totalAnswered = in.readArrayList(Integer.class.getClassLoader());
            lastAnswerPoints = in.readArrayList(Integer.class.getClassLoader());
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(scores);
        dest.writeList(correctAnswers);
        dest.writeList(totalAnswered);
        dest.writeList(lastAnswerPoints);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GameResult> CREATOR = new Creator<>() {
        @Override
        public GameResult createFromParcel(Parcel in) {
            return new GameResult(in);
        }

        @Override
        public GameResult[] newArray(int size) {
            return new GameResult[size];
        }
    };

    public void addAnswer(int player, boolean correct, int points) {
//        System.out.printf("P: %s\nL: %s\nS: %s\n", player+1, points, scores.getOrDefault(player, 0) + points);
        if (correct) correctAnswers.set(player, correctAnswers.get(player) + 1);
        totalAnswered.set(player, totalAnswered.get(player) + 1);
        scores.set(player, scores.get(player) + points);
        lastAnswerPoints.set(player, points);
    }

    // Getters
    public int getScore(int player) { return scores.get(player); }
    public int getCorrectAnswers(int player) { return correctAnswers.get(player); }
    public int getTotalAnswered(int player) { return totalAnswered.get(player); }
    public double getAccuracyPercentage(int player) {
        return totalAnswered.get(player) == 0 ? 0.0 :
            (double) correctAnswers.get(player) / totalAnswered.get(player) * 100.0;
    }
    public List<Integer> getScores() { return scores; }
    public int getLastAnswerPoints(int player) {
        return lastAnswerPoints.get(player);
    }
//
//    private Bundle mapToBundle(Map<Integer, Integer> map) {
//        Bundle b = new Bundle();
//        map.forEach((k, v) -> b.putInt(String.valueOf(k), v));
//        return b;
//    }
//
//    private Map<Integer, Integer> mapFromBundle(Bundle bundle) throws NumberFormatException {
//        Map<Integer, Integer> map = new HashMap<>();
//        bundle.keySet().forEach(k -> map.put(Integer.parseInt(k), bundle.getInt(k)));
//        return map;
//    }
}