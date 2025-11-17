package pl.arczewski.zubrzycki.statki.ui;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import pl.arczewski.zubrzycki.statki.model.Board;
import pl.arczewski.zubrzycki.statki.model.Ship;

public class BoardView {

    private final Board board;
    private final GridPane gridPane = new GridPane();
    private final boolean hideShips;

    public BoardView(Board board, boolean hideShips) {
        this.board = board;
        this.hideShips = hideShips;
        createGrid();
    }

    public BoardView(Board board) {
        this(board, false);
    }

    public GridPane getGridPane() { return gridPane; }

    private void createGrid() {
        gridPane.setHgap(2);
        gridPane.setVgap(2);

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                Rectangle r = new Rectangle(35, 35);
                r.setStroke(Color.BLACK);
                r.setFill(Color.LIGHTBLUE);
                GridPane.setHalignment(r, HPos.CENTER);
                GridPane.setValignment(r, VPos.CENTER);

                gridPane.add(r, x, y);
            }
        }
    }

    public void update() {
        for (Node n : gridPane.getChildren()) {
            int x = GridPane.getColumnIndex(n);
            int y = GridPane.getRowIndex(n);
            Rectangle r = (Rectangle) n;

            var cell = board.getCell(x, y);

            if (cell.isShot()) {
                r.setFill(cell.hasShip() ? Color.RED : Color.GRAY);
            } else {
                if (hideShips) {
                    r.setFill(Color.LIGHTBLUE);
                } else {
                    r.setFill(cell.hasShip() ? Color.DARKBLUE : Color.LIGHTBLUE);
                }
            }
        }
    }

    public Button shipButton(Ship s) {
        Button b = new Button(s.getName() + " (" + s.getSize() + ")");
        b.setPrefWidth(s.getSize() * 35);
        return b;
    }
}
