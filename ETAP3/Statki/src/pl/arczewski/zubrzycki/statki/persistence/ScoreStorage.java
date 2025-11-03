package pl.arczewski.zubrzycki.statki.persistence;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ScoreStorage {
    private final File file = new File("scores.txt");

    public void saveScore(String playerName, int score) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(playerName + ":" + score);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> loadScores() {
        List<String> scores = new ArrayList<>();
        if (!file.exists()) return scores;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                scores.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return scores;
    }
}
