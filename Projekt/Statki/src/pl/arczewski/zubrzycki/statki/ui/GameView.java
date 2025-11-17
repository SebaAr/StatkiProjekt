package pl.arczewski.zubrzycki.statki.ui;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import pl.arczewski.zubrzycki.statki.engine.GameEngine;
import pl.arczewski.zubrzycki.statki.engine.PlayerTurn;
import pl.arczewski.zubrzycki.statki.model.Player;
import pl.arczewski.zubrzycki.statki.model.Ship;

import java.util.ArrayList;
import java.util.List;

public class GameView {

    private final BorderPane root = new BorderPane();
    private final GameEngine engine;
    private final Stage stage;

    private Player placingPlayer;
    private List<Ship> ships;
    private Ship selectedShip;
    private boolean horizontal = true;

    private BoardView p1PlacementView;
    private BoardView p2PlacementView;

    private BoardView p1TargetView;
    private BoardView p2TargetView;

    public GameView(Stage stage, GameEngine engine) {
        this.stage = stage;
        this.engine = engine;
        placingPlayer = engine.getState().getPlayer1();
        showPlacementScreen();
    }

    public Parent getRoot() { return root; }

    //──────────────────────────────────────────────
    //              USTAWIANIE STATKÓW
    //──────────────────────────────────────────────

    private void showPlacementScreen() {

        ships = new ArrayList<>();
        ships.add(new Ship("Carrier", 5));
        ships.add(new Ship("Battleship", 4));
        ships.add(new Ship("Cruiser", 3));
        ships.add(new Ship("Submarine", 3));
        ships.add(new Ship("Destroyer", 2));

        BoardView view = new BoardView(placingPlayer.getBoard());
        if (placingPlayer == engine.getState().getPlayer1()) p1PlacementView = view;
        else p2PlacementView = view;

        VBox right = new VBox(10);
        right.setPadding(new Insets(10));

        for (Ship s : ships) {
            Button b = view.shipButton(s);
            b.setOnAction(e -> selectedShip = s);
            right.getChildren().add(b);
        }

        Button rotate = new Button("Obróć");
        rotate.setOnAction(e -> horizontal = !horizontal);
        right.getChildren().add(rotate);

        Button ready = new Button("Gotowy");
        ready.setOnAction(e -> finishPlacement());
        right.getChildren().add(ready);

        attachPlacementHandlers(view);

        VBox center = new VBox(10,
                new Text("Ustaw statki – " + placingPlayer.getName()),
                view.getGridPane());
        center.setAlignment(Pos.CENTER);

        root.setCenter(center);
        root.setRight(right);
    }

    private void attachPlacementHandlers(BoardView view) {
        for (Node n : view.getGridPane().getChildren()) {

            n.setOnMouseClicked(e -> {
                if (selectedShip == null) return;

                int x = GridPane.getColumnIndex(n);
                int y = GridPane.getRowIndex(n);

                boolean ok = placingPlayer.getBoard().placeShip(selectedShip, x, y, horizontal);

                if (ok) {
                    ships.remove(selectedShip);
                    selectedShip = null;
                    view.update();
                    rebuildShipPanel(view);
                }
            });
        }
    }

    private void rebuildShipPanel(BoardView view) {

        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));

        for (Ship s : ships) {
            Button b = view.shipButton(s);
            b.setOnAction(e -> selectedShip = s);
            rightPanel.getChildren().add(b);
        }

        Button rotate = new Button("Obróć");
        rotate.setOnAction(e -> horizontal = !horizontal);
        rightPanel.getChildren().add(rotate);

        Button ready = new Button("Gotowy");
        ready.setOnAction(e -> finishPlacement());
        rightPanel.getChildren().add(ready);

        root.setRight(rightPanel);
    }

    private void finishPlacement() {

        if (!ships.isEmpty()) {
            root.setTop(new Text("Musisz ustawić wszystkie statki!"));
            return;
        }

        root.setRight(null);
        root.setCenter(new Text("Czekaj na drugiego gracza..."));

        Player p1 = engine.getState().getPlayer1();
        Player p2 = engine.getState().getPlayer2();

        if (placingPlayer == p1) {
            placingPlayer = p2;
            engine.getState().setTurn(PlayerTurn.PLAYER2);
            showPlacementScreen();
            return;
        }

        // START BITWY
        engine.startBattlePhase();
        engine.getState().setTurn(PlayerTurn.PLAYER1);

        p1TargetView = new BoardView(p2.getBoard(), true);
        p2TargetView = new BoardView(p1.getBoard(), true);

        showTurnTransition(engine.getState().getPlayer1());
    }

    //──────────────────────────────────────────────
    //                 PRZEJŚCIE TURY
    //──────────────────────────────────────────────

    private void showTurnTransition(Player p) {

        Text t = new Text("Tura gracza: " + p.getName());
        Button cont = new Button("Kontynuuj");

        cont.setOnAction(e -> showBattleScreen());

        VBox box = new VBox(20, t, cont);
        box.setAlignment(Pos.CENTER);

        root.setCenter(box);
    }

    //──────────────────────────────────────────────
    //                    BITWA
    //──────────────────────────────────────────────

    private void showBattleScreen() {

        Player current = engine.getState().getCurrentPlayer();
        Player opponent = engine.getState().getOtherPlayer();

        BoardView targetView = (current == engine.getState().getPlayer1())
                ? p1TargetView
                : p2TargetView;

        attachShootingHandlers(targetView);
        targetView.update();

        VBox box = new VBox(10,
                new Text("Strzelasz w planszę gracza: " + opponent.getName()),
                targetView.getGridPane());
        box.setAlignment(Pos.CENTER);

        root.setCenter(box);
    }

    private void attachShootingHandlers(BoardView targetView) {

        for (Node n : targetView.getGridPane().getChildren()) {

            n.setOnMouseClicked(e -> {

                int x = GridPane.getColumnIndex(n);
                int y = GridPane.getRowIndex(n);

                boolean hit = engine.shoot(x, y);

                updateTargetViews();

                if (engine.isGameFinished()) {
                    root.setCenter(new Text("KONIEC! Wygrał: " + engine.getWinner().getName()));
                    return;
                }

                if (hit) {
                    showHitMessage();
                } else {
                    showMissMessage();
                }
            });
        }
    }

    //──────────────────────────────────────────────
    //               KOMUNIKAT TRAFIENIA
    //──────────────────────────────────────────────

    private void showHitMessage() {

        Player current = engine.getState().getCurrentPlayer();
        BoardView targetView =
                (current == engine.getState().getPlayer1()) ? p1TargetView : p2TargetView;

        targetView.update();

        Text msg = new Text("TRAFIŁEŚ!");
        msg.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: red;");

        VBox box = new VBox(20, msg, targetView.getGridPane());
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));

        root.setCenter(box);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> showBattleScreen());
        pause.play();
    }

    //──────────────────────────────────────────────
    //               KOMUNIKAT PUDŁA
    //──────────────────────────────────────────────

    private void showMissMessage() {

        Player current = engine.getState().getCurrentPlayer();
        BoardView targetView =
                (current == engine.getState().getPlayer1()) ? p1TargetView : p2TargetView;

        targetView.update();

        Text msg = new Text("PUDŁO!");
        msg.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Button cont = new Button("Kontynuuj");
        cont.setStyle("-fx-font-size: 18px;");

        cont.setOnAction(e -> {
            engine.getState().swapTurn();
            showTurnTransition(engine.getState().getCurrentPlayer());
        });

        VBox box = new VBox(20, msg, targetView.getGridPane(), cont);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));

        root.setCenter(box);
    }

    private void updateTargetViews() {
        if (p1TargetView != null) p1TargetView.update();
        if (p2TargetView != null) p2TargetView.update();
    }
}
