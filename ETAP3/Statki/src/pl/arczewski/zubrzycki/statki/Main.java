package pl.arczewski.zubrzycki.statki;

import javax.swing.*;
import pl.arczewski.zubrzycki.statki.gui.StartPanel;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            StartPanel startMenu = new StartPanel();
            startMenu.setVisible(true);
        });
    }
}
