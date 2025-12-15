package pl.arczewski.zubrzycki.statki.storage;

import javafx.scene.Node;
import javafx.scene.text.Text;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Przechowywanie historii gry + eksport do pliku.
 */
public class HistoryStorage {

    private final List<String> entries = new ArrayList<>();

    public void add(String text) {
        entries.add(text);
    }

    public void clear() {
        entries.clear();
    }

    public List<String> getAll() {
        return new ArrayList<>(entries);
    }

    public File exportToFile(String winnerName) throws Exception {
        String fileName = "Statki - zdarzenia gry " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("[dd-MM-yyyy__HH-mm-ss]")) +
                ".txt";

        File file = new File(fileName);
        PrintWriter writer = new PrintWriter(file, "UTF-8");

        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        writer.println("=== HISTORIA GRY W STATKI ===");
        writer.println("Data: " + formattedDate);
        writer.println("-----------------------------");
        writer.println("Zwycięzca: " + winnerName);
        writer.println();
        writer.println("=== ZDARZENIA W GRZE ===");
        writer.println();

        for (String e : entries) writer.println("• " + e);

        writer.close();
        return file;
    }
}