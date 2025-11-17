package pl.arczewski.zubrzycki.statki.ui;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Button;
import pl.arczewski.zubrzycki.statki.model.Board;
import pl.arczewski.zubrzycki.statki.model.Ship;
import javafx.scene.Node;
import javafx.geometry.HPos;
import javafx.geometry.VPos;

/**
 * Prost y widok planszy 10x10. Nie robi drag&drop — rysuje siatkę i udostępnia helpery.
 * hideShips = true -> statki niewidoczne (plansza przeciwnika).
 */
public class BoardView {

    private final Board board;
    private final boolean hideShips;
    private final GridPane gridPane = new GridPane();
    private final int CELL = 36; // rozmiar komórki w px (dopasuj)

    public BoardView(Board board) {
        this(board, false);
    }

    public BoardView(Board board, boolean hideShips) {
        this.board = board;
        this.hideShips = hideShips;
        createGrid();
    }

    public GridPane getGridPane() { return gridPane; }

    private void createGrid() {
        gridPane.setHgap(2);
        gridPane.setVgap(2);
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Rectangle r = new Rectangle(CELL, CELL);
                r.setStroke(Color.BLACK);
                r.setFill(Color.LIGHTBLUE);
                GridPane.setHalignment(r, HPos.CENTER);
                GridPane.setValignment(r, VPos.CENTER);
                gridPane.add(r, col, row);
            }
        }
        updateGrid();
    }

    /**
     * Odświeża kolory na planszy zgodnie z modelami (shot / ship / hidden)
     */
    public void updateGrid() {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                Rectangle rect = (Rectangle) getNodeByRowColumnIndex(y, x);
                if (rect == null) continue;

                if (board.getCell(x,y).isShot()) {
                    if (board.getCell(x,y).hasShip()) rect.setFill(Color.RED);
                    else rect.setFill(Color.GRAY);
                } else {
                    if (board.getCell(x,y).hasShip() && !hideShips) rect.setFill(Color.DARKBLUE);
                    else rect.setFill(Color.LIGHTBLUE);
                }
            }
        }
    }

    private Node getNodeByRowColumnIndex(final int row, final int column) {
        for (Node node : gridPane.getChildren()) {
            Integer r = GridPane.getRowIndex(node);
            Integer c = GridPane.getColumnIndex(node);
            int rn = r == null ? 0 : r;
            int cn = c == null ? 0 : c;
            if (rn == row && cn == column) return node;
        }
        return null;
    }

    /**
     * Tworzy przycisk reprezentujący statek (do wyboru przed ustawieniem).
     * GameView będzie używać tego przycisku: klik -> selectedShip = ship.
     */
    public Button createShipButton(Ship ship) {
        Button btn = new Button(ship.getName() + " (" + ship.getSize() + ")");
        btn.setPrefWidth(ship.getSize() * (CELL));
        return btn;
    }
}
