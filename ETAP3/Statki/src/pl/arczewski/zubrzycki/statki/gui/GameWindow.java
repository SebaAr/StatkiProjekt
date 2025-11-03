package pl.arczewski.zubrzycki.statki.gui;


import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {

    public GameWindow() {
        setTitle("Statki - Gra");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Ekran gry
        add(new BoardPanel());

        setVisible(true);
    }
}
