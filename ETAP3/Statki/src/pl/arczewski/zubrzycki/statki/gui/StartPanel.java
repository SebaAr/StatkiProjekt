package pl.arczewski.zubrzycki.statki.gui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class StartPanel extends JFrame {

    public StartPanel() {
        setTitle("Statki - Menu Startowe");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JButton startButton = new JButton("Rozpocznij grę");
        startButton.addActionListener((ActionEvent e) -> {
            dispose();
            new GameWindow();
        });

        setLayout(new BorderLayout());
        add(new JLabel("Witaj w grze STATKI!", SwingConstants.CENTER), BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);
    }
}
