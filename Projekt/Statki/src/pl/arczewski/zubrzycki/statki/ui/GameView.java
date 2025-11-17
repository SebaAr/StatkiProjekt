package pl.arczewski.zubrzycki.statki.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pl.arczewski.zubrzycki.statki.engine.GameEngine;
import pl.arczewski.zubrzycki.statki.model.Board;
import pl.arczewski.zubrzycki.statki.model.Player;
import pl.arczewski.zubrzycki.statki.model.Ship;

import java.util.ArrayList;
import java.util.List;

/**
 * GameView — poprawiona wersja: wybór statku przez przyciski (klik), klik w pole ustawia statek.
 * Następnie "Gotowy" przełącza na drugiego gracza, po ustawieniu obu start gry.
 * W fazie gry: klik na planszę przeciwnika -> strzał; pudło -> ekran przejścia (swap),
 * trafienie -> kontynuacja bieżącego gracza.
 */
public class GameView {

    private final BorderPane root = new BorderPane();
    private final GameEngine engine;

    private BoardView playerBoardView;
    private BoardView opponentBoardView;

    private Player currentSetupPlayer;
    private boolean setupPhase = true;

    private VBox shipPanel;
    private Button readyButton;
    private List<Ship> shipsToPlace;

    private Stage stage;
    private Text gameStatus;

    // selectedShip — statek aktualnie wybrany przez przycisk do ustawienia
    private Ship selectedShip = null;

    // orientacja ustawiania (true = horizontal, false = vertical)
    private boolean placingHorizontal = true;

    public GameView(Stage stage, GameEngine engine) {
        this.stage = stage;
        this.engine = engine;
        initSetupPhase(engine.getState().getCurrentPlayer());
    }

    public Parent getRoot() { return root; }

    // ----------------------- Faza ustawiania statków -----------------------
    private void initSetupPhase(Player player) {
        currentSetupPlayer = player;

        playerBoardView = new BoardView(player.getBoard(), false);

        shipPanel = new VBox(8);
        shipPanel.setPadding(new Insets(10));
        shipPanel.setAlignment(Pos.CENTER);

        // tworzymy listę statków (możesz zmienić nazwy)
        shipsToPlace = new ArrayList<>();
        shipsToPlace.add(new Ship("Carrier", 5));
        shipsToPlace.add(new Ship("Battleship", 4));
        shipsToPlace.add(new Ship("Cruiser", 3));
        shipsToPlace.add(new Ship("Submarine", 3));
        shipsToPlace.add(new Ship("Destroyer", 2));

        shipPanel.getChildren().clear();
        for (Ship s : shipsToPlace) {
            Button b = playerBoardView.createShipButton(s);
            // klik na przycisk -> wybierz statek do ustawienia
            b.setOnAction(e -> {
                selectedShip = s;
                gameStatus = new Text("Wybrano: " + s.getName() + " (" + s.getSize() + "). Kliknij pole planszy, aby ustawić. Orientacja: " + (placingHorizontal ? "pozioma" : "pionowa"));
                root.setTop(gameStatus);
            });
            // prawy klik (alternatywnie kliknięcie z modyfikatorem) obraca orientację
            b.setOnMouseClicked(e -> {
                if (e.isShiftDown()) { // shift+click obraca (opcjonalnie)
                    placingHorizontal = !placingHorizontal;
                    gameStatus = new Text("Orientacja zmieniona: " + (placingHorizontal ? "pozioma" : "pionowa"));
                    root.setTop(gameStatus);
                }
            });
            shipPanel.getChildren().add(b);
        }

        readyButton = new Button("Gotowy");
        readyButton.setOnAction(e -> proceedToNextPlayer());

        Button rotateBtn = new Button("Obróć kierunek (Shift+klik też obraca)");
        rotateBtn.setOnAction(e -> {
            placingHorizontal = !placingHorizontal;
            gameStatus = new Text("Orientacja zmieniona: " + (placingHorizontal ? "pozioma" : "pionowa"));
            root.setTop(gameStatus);
        });

        VBox rightPanel = new VBox(12, new Text("Ustaw statki dla " + player.getName()), shipPanel, rotateBtn, readyButton);
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setPadding(new Insets(10));

        // centralnie pokazujemy własną planszę do ustawiania
        root.setCenter(playerBoardView.getGridPane());
        root.setRight(rightPanel);

        // ustawienie kliknięć na własnej planszy: klik -> próba ustawienia selectedShip
        attachPlacementHandlers(playerBoardView, player);
        // status
        root.setTop(new Text("Ustawianie statków dla " + player.getName() + ". Wybierz statek z prawego panelu."));
    }

    private void attachPlacementHandlers(BoardView boardView, Player player) {
        GridPane gp = boardView.getGridPane();
        gp.getChildren().forEach(node -> {
            node.setOnMouseClicked(ev -> {
                if (!setupPhase) return;
                if (selectedShip == null) {
                    root.setTop(new Text("Wybierz najpierw statek z prawego panelu."));
                    return;
                }
                Integer row = GridPane.getRowIndex(node);
                Integer col = GridPane.getColumnIndex(node);
                int r = (row == null) ? 0 : row;
                int c = (col == null) ? 0 : col;
                // spróbuj ustawić statek w modelu
                boolean placed = player.getBoard().placeShip(selectedShip, c, r, placingHorizontal);
                if (placed) {
                    // wyłącz przycisk (usuń z panelu) - proste rozwiązanie: usuwamy selectedShip z listy i odświeżamy panel
                    shipsToPlace.remove(selectedShip);
                    selectedShip = null;
                    boardView.updateGrid();
                    rebuildShipPanel(); // usuwa przyciski już ustawionych statków
                    root.setTop(new Text("Statek ustawiony. Kontynuuj lub kliknij Gotowy."));
                } else {
                    root.setTop(new Text("Nie można ustawić statku w tym miejscu. Spróbuj innego pola lub obróć."));
                }
            });
        });
    }

    private void rebuildShipPanel() {
        shipPanel.getChildren().clear();
        for (Ship s : shipsToPlace) {
            Button b = playerBoardView.createShipButton(s);
            b.setOnAction(e -> {
                selectedShip = s;
                root.setTop(new Text("Wybrano: " + s.getName()));
            });
            shipPanel.getChildren().add(b);
        }
    }

    private void proceedToNextPlayer() {
        // jeśli nadal są statki do ustawienia, wymusimy ustawienie wszystkich
        if (!shipsToPlace.isEmpty()) {
            root.setTop(new Text("Musisz ustawić wszystkie statki przed kliknięciem Gotowy."));
            return;
        }

        // jeśli current setup player jest pierwszy, przechodzimy do drugiego
        Player other = engine.getState().getOtherPlayer();
        if (engine.getState().getCurrentPlayer() == currentSetupPlayer && other != null) {
            // przełączenie modelu tak, żeby drugi gracz był current (ale nie zmieniamy tury w silniku)
            // swap roles in state so initSetupPhase uses other player as current
            engine.getState().swapTurn(); // teraz current = other
            initSetupPhase(engine.getState().getCurrentPlayer());
        } else {
            // koniec ustawiania obu graczy: ustaw stan na pierwszy gracz (zachowamy who starts)
            // jeśli swapTurn spowodował, że current jest drugim, to znów przywracamy
            if (engine.getState().getCurrentPlayer() != currentSetupPlayer) {
                engine.getState().swapTurn(); // przywróć porządek tak, że current = pierwszy gracz
            }
            setupPhase = false;
            showTransitionScreen(engine.getState().getCurrentPlayer());
        }
    }

    // ----------------------- Ekran przejścia -----------------------
    private void showTransitionScreen(Player nextPlayer) {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(16));
        Text t = new Text("Teraz ruch gracza: " + nextPlayer.getName() + "\nKliknij Kontynuuj, aby zobaczyć swoją planszę.");
        Button cont = new Button("Kontynuuj");
        cont.setOnAction(e -> startGame());
        box.getChildren().addAll(t, cont);
        root.setCenter(box);
        root.setRight(null);
        root.setTop(null);
    }

    // ----------------------- Faza gry -----------------------
    private void startGame() {
        Player current = engine.getState().getCurrentPlayer();
        Player opponent = engine.getState().getOtherPlayer();

        playerBoardView = new BoardView(current.getBoard(), false);
        opponentBoardView = new BoardView(opponent.getBoard(), true);

        VBox left = new VBox(8, new Text(current.getName() + " - Twoja plansza"), playerBoardView.getGridPane());
        VBox right = new VBox(8, new Text("Plansza przeciwnika"), opponentBoardView.getGridPane());
        left.setAlignment(Pos.CENTER);
        right.setAlignment(Pos.CENTER);

        HBox center = new HBox(24, left, right);
        center.setAlignment(Pos.CENTER);

        gameStatus = new Text(current.getName() + ", Twój ruch!");
        VBox main = new VBox(12, gameStatus, center);
        main.setAlignment(Pos.CENTER);
        main.setPadding(new Insets(10));

        root.setCenter(main);
        root.setRight(null);
        root.setTop(null);

        attachShootingHandlers();
        updateBoards();
    }

    private void attachShootingHandlers() {
        // kliknięcia tylko w planszę przeciwnika
        GridPane gp = opponentBoardView.getGridPane();
        gp.getChildren().forEach(node -> node.setOnMouseClicked(ev -> {
            Integer row = GridPane.getRowIndex(node);
            Integer col = GridPane.getColumnIndex(node);
            int r = (row == null) ? 0 : row;
            int c = (col == null) ? 0 : col;
            handleShot(c, r);
        }));
    }

    private void handleShot(int x, int y) {
        if (engine.isGameFinished()) return;

        boolean hit = engine.shoot(x, y);
        updateBoards();

        if (hit) {
            gameStatus.setText("Trafienie! " + engine.getState().getCurrentPlayer().getName() + " strzela ponownie.");
        } else {
            // pudło -> zmiana gracza i ekran przejścia
            engine.getState().swapTurn();
            showTransitionScreen(engine.getState().getCurrentPlayer());
        }

        if (engine.isGameFinished()) {
            gameStatus.setText("Koniec gry! Zwycięzca: " + engine.getWinner().getName());
            // Możesz też wyświetlić Alert lub przycisk restartuj
        }
    }

    private void updateBoards() {
        // player view
        playerBoardView.updateGrid();
        // opponent view
        opponentBoardView.updateGrid();
    }
}
