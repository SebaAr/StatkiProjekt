package pl.arczewski.zubrzycki.statki.gui;


import javax.swing.*;
import java.awt.*;

public class BoardPanel extends JPanel {

    private static final int GRID_SIZE = 10;
    private static final int CELL_SIZE = 40;

    public BoardPanel() {
        setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));
        setBackground(Color.CYAN);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Rysowanie siatki
        g.setColor(Color.BLACK);
        for (int i = 0; i <= GRID_SIZE; i++) {
            g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, GRID_SIZE * CELL_SIZE);
            g.drawLine(0, i * CELL_SIZE, GRID_SIZE * CELL_SIZE, i * CELL_SIZE);
        }
    }
}
