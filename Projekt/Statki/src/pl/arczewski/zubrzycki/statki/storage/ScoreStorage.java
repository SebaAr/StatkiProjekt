package pl.arczewski.zubrzycki.statki.storage;


import java.io.*;
import java.util.*;

public class ScoreStorage {

    private static final String FILE = "scores.dat";

    public static void saveScore(String player, int score) throws IOException {
        Map<String,Integer> scores = loadScores();
        scores.put(player, score);
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE))) {
            oos.writeObject(scores);
        }
    }

    public static Map<String,Integer> loadScores() throws IOException {
        File f = new File(FILE);
        if(!f.exists()) return new HashMap<>();
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            try { return (Map<String,Integer>) ois.readObject(); }
            catch(ClassNotFoundException e) { return new HashMap<>(); }
        }
    }
}
