package pl.arczewski.zubrzycki.statki.ui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import pl.arczewski.zubrzycki.statki.engine.GameEngine;
import pl.arczewski.zubrzycki.statki.engine.PlayerTurn;
import pl.arczewski.zubrzycki.statki.model.*;
import pl.arczewski.zubrzycki.statki.network.*;
import pl.arczewski.zubrzycki.statki.protocol.*;
import pl.arczewski.zubrzycki.statki.storage.HistoryStorage;

import java.util.*;

public class GameView implements GameNetworkListener {

    private boolean inputLocked = false;

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

    // UI historii
    private final VBox historyBox = new VBox(5);
    private final ScrollPane historyScroll = new ScrollPane();

    // NOWY STORAGE
    private final HistoryStorage history = new HistoryStorage();

    private final VBox warningBox = new VBox();
    private final Text warningText = new Text("Musisz ustawić wszystkie statki!");

    private final Map<Player, Integer> usedBoostCount = new HashMap<>();
    private final Map<Player, Set<BoostType>> usedBoosts = new HashMap<>();

    private boolean swapMode = false;
    private Ship swapTarget = null;

    private final GameServer netServer;
    private final GameClient netClient;
    private final boolean onlineMode;

    public GameView(Stage stage, GameEngine engine) {
        this(stage, engine, null, null);
    }

    public GameView(Stage stage,
                    GameEngine engine,
                    GameServer netServer,
                    GameClient netClient) {

        this.stage = stage;
        this.engine = engine;
        this.netServer = netServer;
        this.netClient = netClient;

        this.onlineMode = (netServer != null || netClient != null);
        placingPlayer = engine.getState().getPlayer1();

        setupHistoryPane();
        setupWarningPane();
        initBoostState();

        if (netServer != null) netServer.setListener(this);
        if (netClient != null) netClient.setListener(this);

        showPlacementScreen();
    }

    public Parent getRoot() {
        return root;
    }

    private void setupHistoryPane() {
        historyBox.setPadding(new Insets(10));
        historyScroll.setContent(historyBox);
        historyScroll.setFitToWidth(true);
        historyScroll.setPrefWidth(265);
        historyScroll.getStyleClass().add("history-pane");
        historyBox.getStyleClass().add("history-pane");

        historyBox.heightProperty().addListener((obs, oldVal, newVal) -> {
            historyScroll.setVvalue(1.0);
        });
    }


    private void setupWarningPane() {
        warningText.setStyle("-fx-fill: #aa0000; -fx-font-size: 16px; -fx-font-weight: bold;");
        warningBox.getChildren().add(warningText);
        warningBox.setPadding(new Insets(10));
        warningBox.setAlignment(Pos.CENTER_RIGHT);
        warningBox.setVisible(false);
    }

    private void initBoostState() {
        Player p1 = engine.getState().getPlayer1();
        Player p2 = engine.getState().getPlayer2();

        usedBoostCount.put(p1, 0);
        usedBoostCount.put(p2, 0);

        usedBoosts.put(p1, EnumSet.noneOf(BoostType.class));
        usedBoosts.put(p2, EnumSet.noneOf(BoostType.class));
    }

    // ======= HISTORIA GRY =======
    private void addHistoryEntry(String text) {
        history.add(text);

        Text t = new Text(text);
        t.getStyleClass().add("history-entry");
        historyBox.getChildren().add(t);
    }

    // ============================
    // FAZA USTAWIANIA
    // ============================

    private List<Ship> createDefaultShips() {
        return Arrays.asList(
                new Ship("Lotniskowiec", 5),
                new Ship("Okręt wojenny", 4),
                new Ship("Niszczyciel", 3),
                new Ship("Krążownik", 2),
                new Ship("Batyskaf", 1)
        );
    }

    private void resetShips() {
        ships = new ArrayList<>(createDefaultShips());
        selectedShip = ships.get(0);
    }

    private void autoPlaceForCurrentPlayer() {
        var board = placingPlayer.getBoard();
        board.clear();

        List<Ship> autoShips = createDefaultShips();
        Random r = new Random();

        for (Ship s : autoShips) {
            boolean placed = false;
            while (!placed) {
                boolean horiz = r.nextBoolean();
                int x = r.nextInt(10);
                int y = r.nextInt(10);
                if (board.placeShip(s, x, y, horiz)) placed = true;
            }
        }
    }

    private void showPlacementScreen() {

        resetShips();
        warningBox.setVisible(false);

        if (placingPlayer instanceof BotPlayer) {
            autoPlaceForCurrentPlayer();
            ships.clear();
            selectedShip = null;
            finishPlacement();
            return;
        }

        BoardView view = new BoardView(placingPlayer.getBoard());
        if (placingPlayer == engine.getState().getPlayer1()) {
            p1PlacementView = view;
        } else {
            p2PlacementView = view;
        }

        attachPlacementHandlers(view);

        Text title = new Text("Ustaw statki – " + placingPlayer.getName());
        title.getStyleClass().addAll("player-name", "player-name-placement");

        VBox center = new VBox(10, title, view.getGridPane());
        center.setAlignment(Pos.CENTER);

        // --- NOWE, WAŻNE, WYŚRODKOWANIE ---
        HBox centerWrapper = new HBox(center);
        centerWrapper.setAlignment(Pos.CENTER);
        centerWrapper.setFillHeight(true);
        centerWrapper.setPrefWidth(Double.MAX_VALUE);
        // ----------------------------------

        rebuildShipPanel(view);

        root.setCenter(centerWrapper);
    }

    private void selectShipFromButton(Ship ship, VBox rightPanel) {
        selectedShip = ship;

        for (Node n : rightPanel.getChildren()) {
            if (n instanceof Button b) {
                Object ud = b.getUserData();
                if (ud instanceof Ship s) {
                    b.getStyleClass().remove("ship-button-selected");
                    if (s == ship) b.getStyleClass().add("ship-button-selected");
                }
            }
        }
    }

    private void attachPlacementHandlers(BoardView view) {

        var board = placingPlayer.getBoard();

        for (Node n : view.getGridPane().getChildren()) {

            n.setOnMouseEntered(event -> {

                Integer gx = GridPane.getColumnIndex(n);
                Integer gy = GridPane.getRowIndex(n);

                if (gx == null || gy == null) {
                    view.clearPreview();
                    return;
                }

                int x = gx - 1;
                int y = gy - 1;

                if (x < 0 || y < 0) {
                    view.clearPreview();
                    return;
                }

                var cell = board.getCell(x, y);

                if (cell.hasShip()) {
                    highlightWholeShip(view, cell.getShip());
                    return;
                }

                if (selectedShip == null) {
                    view.clearPreview();
                    return;
                }

                boolean can = board.canPlaceShipWithSpacing(
                        x, y,
                        selectedShip.getSize(),
                        horizontal
                );

                view.showPreview(selectedShip, x, y, horizontal, can);
            });

            n.setOnMouseExited(event -> view.clearPreview());

            n.setOnMouseClicked(event -> {

                Integer gx = GridPane.getColumnIndex(n);
                Integer gy = GridPane.getRowIndex(n);
                if (gx == null || gy == null) return;

                int x = gx - 1;
                int y = gy - 1;

                if (x < 0 || y < 0) return;

                var cell = board.getCell(x, y);

                if (cell.hasShip()) {

                    Ship ship = cell.getShip();
                    board.removeShip(ship);

                    if (!ships.contains(ship)) ships.add(ship);
                    selectedShip = ship;

                    view.update();
                    rebuildShipPanel(view);
                    return;
                }

                if (selectedShip == null) return;

                boolean ok = board.placeShip(selectedShip, x, y, horizontal);

                if (ok) {
                    ships.remove(selectedShip);
                    selectedShip = ships.isEmpty() ? null : ships.get(0);

                    view.update();
                    rebuildShipPanel(view);
                    if (ships.isEmpty()) warningBox.setVisible(false);
                }
            });
        }
    }

    private void rebuildShipPanel(BoardView view) {

        VBox shipList = new VBox(10);
        shipList.setPadding(new Insets(10));
        shipList.setAlignment(Pos.CENTER);

        for (Ship s : ships) {
            Button b = view.shipButton(s);
            b.setUserData(s);
            if (!b.getStyleClass().contains("ship-button"))
                b.getStyleClass().add("ship-button");

            if (s == selectedShip)
                b.getStyleClass().add("ship-button-selected");
            else
                b.getStyleClass().remove("ship-button-selected");

            b.setOnAction(event -> selectShipFromButton(s, shipList));
            shipList.getChildren().add(b);
        }

        Button rotate = new Button("Obróć");
        rotate.setPrefWidth(200);
        rotate.setOnAction(event -> horizontal = !horizontal);

        Button auto = new Button("Auto");
        auto.setPrefWidth(200);
        auto.setOnAction(event -> autoPlaceShips(view));

        Button clear = new Button("Wyczyść");
        clear.setPrefWidth(200);
        clear.setOnAction(event -> {
            clearBoard(view);
            warningBox.setVisible(false);
        });

        Button ready = new Button("Gotowy");
        ready.setPrefWidth(200);
        ready.setOnAction(event -> finishPlacement());

        VBox bottomButtons = new VBox(10, rotate, auto, clear, ready);
        bottomButtons.setAlignment(Pos.CENTER);

        VBox bottomArea = new VBox(10, bottomButtons, warningBox);
        bottomArea.setAlignment(Pos.CENTER);

        BorderPane side = new BorderPane();
        side.setTop(shipList);
        side.setBottom(bottomArea);

        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);

        BorderPane wrapper = new BorderPane();
        wrapper.setLeft(sep);
        wrapper.setCenter(side);

        root.setRight(wrapper);
    }

    private void autoPlaceShips(BoardView view) {

        var board = placingPlayer.getBoard();
        board.clear();

        List<Ship> autoShips = createDefaultShips();
        Random r = new Random();

        for (Ship s : autoShips) {
            boolean placed = false;

            while (!placed) {
                boolean horiz = r.nextBoolean();
                int x = r.nextInt(10);
                int y = r.nextInt(10);

                if (board.placeShip(s, x, y, horiz)) {
                    placed = true;
                }
            }
        }

        ships.clear();
        selectedShip = null;

        view.update();
        rebuildShipPanel(view);
        warningBox.setVisible(false);
    }

    private void clearBoard(BoardView view) {
        placingPlayer.getBoard().clear();
        ships = new ArrayList<>(createDefaultShips());
        selectedShip = ships.get(0);

        view.update();
        rebuildShipPanel(view);
        warningBox.setVisible(false);
    }

    private void finishPlacement() {
        if (!ships.isEmpty()) {
            warningBox.setVisible(true);
            return;
        }

        warningBox.setVisible(false);
        root.setBottom(null);

        Player p1 = engine.getState().getPlayer1();
        Player p2 = engine.getState().getPlayer2();

        if (placingPlayer == p1) {
            placingPlayer = p2;
            engine.getState().setTurn(PlayerTurn.PLAYER2);

            root.setRight(null);
            root.setCenter(new Text("Czekaj na drugiego gracza..."));

            showPlacementScreen();
            return;
        }

        engine.getState().setTurn(PlayerTurn.PLAYER1);

        historyBox.getChildren().clear();

        p1TargetView = new BoardView(p2.getBoard(), true);
        p2TargetView = new BoardView(p1.getBoard(), true);

        p1PlacementView = new BoardView(p1.getBoard());
        p2PlacementView = new BoardView(p2.getBoard());

        inputLocked = false;
        showBattleScreen();
    }

    // ===========================
    // FAZA WALKI
    // ===========================

    private void showBattleScreen() {
        Player current = engine.getState().getCurrentPlayer();
        Player opponent = engine.getState().getOtherPlayer();

        BoardView myBoardView =
                (current == engine.getState().getPlayer1())
                        ? p1PlacementView
                        : p2PlacementView;

        BoardView targetView =
                (current == engine.getState().getPlayer1())
                        ? p1TargetView
                        : p2TargetView;

        attachShootingHandlers(targetView);
        attachSwapHandlers(myBoardView);

        targetView.update();
        myBoardView.update();

        Text myLabel = new Text("Twoja plansza: " + current.getName());
        myLabel.getStyleClass().addAll("player-name", "player-name-current");

        Text enemyLabel = new Text("Plansza przeciwnika: " + opponent.getName());
        enemyLabel.getStyleClass().addAll("player-name", "player-name-enemy");

        VBox leftPane = new VBox(10, myLabel, myBoardView.getGridPane());
        leftPane.setAlignment(Pos.CENTER);

        VBox rightPane = new VBox(10, enemyLabel, targetView.getGridPane());
        rightPane.setAlignment(Pos.CENTER);

        Rectangle sep = new Rectangle(3, 600);
        sep.getStyleClass().add("board-separator");

        VBox sepWrapper = new VBox(sep);
        sepWrapper.setAlignment(Pos.CENTER);

        HBox boards = new HBox(40, leftPane, sepWrapper, rightPane);
        boards.setAlignment(Pos.CENTER);

        root.setCenter(boards);

        historyScroll.setFitToWidth(true);
        historyScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(historyScroll, Priority.ALWAYS);

        VBox historyWrapper = new VBox(historyScroll);
        root.setRight(historyWrapper);

        if (!inputLocked
                && engine.getState().isBoostsEnabled()
                && !(current instanceof BotPlayer)
                && !(opponent instanceof BotPlayer)) {

            HBox boostBar = new HBox(10);
            boostBar.setStyle("-fx-background-color: #ddeeff;");
            boostBar.setAlignment(Pos.CENTER_LEFT);
            boostBar.setPadding(new Insets(10));

            Button radarBtn = new Button("RADAR");
            Button swapBtn = new Button("SWAP");
            Button trollBtn = new Button("TROLL");

            int used = getUsedBoostCount(current);
            Set<BoostType> usedSet = getUsedBoosts(current);
            boolean blockAll = used >= 2;

            radarBtn.setDisable(blockAll || usedSet.contains(BoostType.RADAR));
            swapBtn.setDisable(blockAll || usedSet.contains(BoostType.SWAP));
            trollBtn.setDisable(blockAll || usedSet.contains(BoostType.TROLL));

            radarBtn.setOnAction(event -> {
                useRadar();
                showBattleScreen();
            });

            swapBtn.setOnAction(event -> {
                useSwap();
                showBattleScreen();
            });

            trollBtn.setOnAction(event -> {
                useTroll();
                showBattleScreen();
            });

            boostBar.getChildren().addAll(radarBtn, swapBtn, trollBtn);
            root.setBottom(boostBar);

        } else {
            root.setBottom(null);
        }
    }

    private void refreshBoostBar() {
        showBattleScreen();
    }

    // ==================================
    // HANDLERY STRZELANIA
    // ==================================

    private void attachShootingHandlers(BoardView targetView) {

        for (Node n : targetView.getGridPane().getChildren()) {

            n.setOnMouseClicked(event -> {

                if (inputLocked) return;

                Integer gx = GridPane.getColumnIndex(n);
                Integer gy = GridPane.getRowIndex(n);
                if (gx == null || gy == null) return;

                int x = gx - 1;
                int y = gy - 1;
                if (x < 0 || y < 0) return;

                Player current = engine.getState().getCurrentPlayer();
                Player opponent = engine.getState().getOtherPlayer();

                boolean hit = engine.shoot(x, y);
                updateTargetViews();

                String coord = "" + (char) ('A' + y) + (x + 1);

                if (hit) {
                    addHistoryEntry(current.getName() + ": " + coord + " – Trafienie");

                    var cell = opponent.getBoard().getCell(x, y);
                    if (cell.hasShip() && cell.getShip().isSunk())
                        addHistoryEntry("Zatopiony: " + cell.getShip().getName());

                    if (engine.isGameFinished()) {
                        root.setBottom(null);
                        showEndScreen();
                        return;
                    }

                    showBattleScreen();
                    return;
                }

                addHistoryEntry(current.getName() + ": " + coord + " – Pudło");
                inputLocked = true;

                if (engine.isGameFinished()) {
                    showEndScreen();
                    return;
                }

                engine.getState().swapTurn();

                if (engine.getState().getCurrentPlayer() instanceof BotPlayer) {

                    inputLocked = true;

                    PauseTransition pause = new PauseTransition(Duration.seconds(1.0));
                    pause.setOnFinished(ev -> botMove());
                    pause.play();

                    return;
                }

                inputLocked = false;
                showTurnOverlay(engine.getState().getCurrentPlayer());

            });
        }
    }

    // ==================================
    // BOOSTY
    // ==================================

    private int getUsedBoostCount(Player p) {
        return usedBoostCount.getOrDefault(p, 0);
    }

    private Set<BoostType> getUsedBoosts(Player p) {
        return usedBoosts.computeIfAbsent(p, k -> EnumSet.noneOf(BoostType.class));
    }

    private boolean canUseBoost(Player p, BoostType type) {
        if (getUsedBoostCount(p) >= 2) return false;
        return !getUsedBoosts(p).contains(type);
    }

    private void markBoostUsed(Player p, BoostType type) {
        Set<BoostType> set = getUsedBoosts(p);
        if (!set.contains(type)) {
            set.add(type);
            usedBoostCount.put(p, getUsedBoostCount(p) + 1);
        }
    }

    private VBox createBoostButtons() {

        Player current = engine.getState().getCurrentPlayer();

        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));

        Text title = new Text("Boosty (max 2)");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button radarBtn = new Button("RADAR");
        Button swapBtn = new Button("SWAP");
        Button trollBtn = new Button("TROLL");

        int used = getUsedBoostCount(current);
        Set<BoostType> usedSet = getUsedBoosts(current);
        boolean blockAll = used >= 2;

        radarBtn.setDisable(blockAll || usedSet.contains(BoostType.RADAR));
        swapBtn.setDisable(blockAll || usedSet.contains(BoostType.SWAP));
        trollBtn.setDisable(blockAll || usedSet.contains(BoostType.TROLL));

        radarBtn.setOnAction(event -> useRadar());
        swapBtn.setOnAction(event -> useSwap());
        trollBtn.setOnAction(event -> useTroll());

        box.getChildren().addAll(title, radarBtn, swapBtn, trollBtn);
        return box;
    }

    // ===== RADAR =====

    private void useRadar() {
        Player current = engine.getState().getCurrentPlayer();
        Player opponent = engine.getState().getOtherPlayer();

        if (!canUseBoost(current, BoostType.RADAR)) return;

        if (onlineMode) {
            try {
                BoostEvent cmd = new BoostEvent(
                        BoostEvent.Kind.RADAR,
                        -1, -1,
                        0, false
                );

                if (netServer != null) netServer.send(cmd);
                if (netClient != null) netClient.send(cmd);

            } catch (Exception ignored) {}
        }

        var cell = opponent.getBoard().revealRandomShipCell();
        if (cell == null) return;

        int fx = -1, fy = -1;

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                if (opponent.getBoard().getCell(x, y) == cell) {
                    fx = x;
                    fy = y;
                    break;
                }
            }
        }

        if (fx != -1) {
            String coord = "" + (char) ('A' + fy) + (fx + 1);
            addHistoryEntry(current.getName() + " : namierza pole " + coord);
        }

        markBoostUsed(current, BoostType.RADAR);
        root.setBottom(createBoostButtons());
    }

    // ===== SWAP =====

    private void useSwap() {
        Player current = engine.getState().getCurrentPlayer();

        if (!canUseBoost(current, BoostType.SWAP)) return;

        swapMode = true;
        swapTarget = null;

        markBoostUsed(current, BoostType.SWAP);

        addHistoryEntry(current.getName() + " : zmienia położenie statku");
        addHistoryEntry("Gra: wybierz statek");

        root.setBottom(createBoostButtons());
    }

    private void attachSwapHandlers(BoardView view) {

        var board = engine.getState().getCurrentPlayer().getBoard();

        for (Node n : view.getGridPane().getChildren()) {

            n.setOnMouseEntered(event -> {

                if (!swapMode) return;

                Integer gx = GridPane.getColumnIndex(n);
                Integer gy = GridPane.getRowIndex(n);
                if (gx == null || gy == null) return;

                int x = gx - 1;
                int y = gy - 1;

                if (swapTarget == null) {

                    var cell = board.getCell(x, y);
                    if (cell.hasShip()) highlightSwapShip(view, cell.getShip());
                    else view.clearPreview();
                    return;
                }

                boolean can = board.canPlaceShipWithSpacing(
                        x, y,
                        swapTarget.getSize(),
                        swapTarget.isHorizontal()
                );

                view.showPreview(
                        swapTarget,
                        x,
                        y,
                        swapTarget.isHorizontal(),
                        can
                );
            });

            n.setOnMouseExited(event -> {
                if (swapMode) view.update();
            });

            n.setOnMouseClicked(event -> {

                if (!swapMode) return;

                Integer gx = GridPane.getColumnIndex(n);
                Integer gy = GridPane.getRowIndex(n);
                if (gx == null || gy == null) return;

                int x = gx - 1;
                int y = gy - 1;

                if (swapTarget == null) {

                    var cell = board.getCell(x, y);
                    if (!cell.hasShip()) return;

                    swapTarget = cell.getShip();
                    addHistoryEntry("Gra: wybrano " + swapTarget.getName());
                    highlightSwapShip(view, swapTarget);
                    return;
                }

                boolean ok = board.moveShip(
                        swapTarget,
                        x, y,
                        swapTarget.isHorizontal()
                );

                if (onlineMode) {
                    try {
                        BoostEvent cmd = new BoostEvent(
                                BoostEvent.Kind.SWAP_MOVE,
                                x, y,
                                swapTarget.getSize(),
                                swapTarget.isHorizontal()
                        );

                        if (netServer != null) netServer.send(cmd);
                        if (netClient != null) netClient.send(cmd);
                    } catch (Exception ignored) {}
                }

                if (!ok) {
                    addHistoryEntry("Gra: wybierz inne miejsce");
                    return;
                }

                addHistoryEntry("Gra: statek przeniesiony");

                swapMode = false;
                swapTarget = null;

                view.update();
            });
        }
    }

    private void highlightSwapShip(BoardView view, Ship ship) {

        view.update();
        var board = engine.getState().getCurrentPlayer().getBoard();

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {

                var cell = board.getCell(x, y);
                if (cell.getShip() != ship) continue;

                int gx = x + 1;
                int gy = y + 1;

                for (Node n : view.getGridPane().getChildren()) {

                    Integer cx = GridPane.getColumnIndex(n);
                    Integer cy = GridPane.getRowIndex(n);
                    if (cx == null || cy == null) continue;
                    if (!(n instanceof Rectangle r)) continue;

                    if (cx == gx && cy == gy) {
                        r.getStyleClass().removeAll(
                                "cell-water",
                                "cell-ship",
                                "cell-hit",
                                "cell-miss",
                                "cell-preview-ok",
                                "cell-preview-bad"
                        );
                        r.getStyleClass().add("cell-preview-bad");
                    }
                }
            }
        }
    }

    // ===== TROLL =====

    private void useTroll() {
        Player current = engine.getState().getCurrentPlayer();

        if (!canUseBoost(current, BoostType.TROLL)) return;

        var board = current.getBoard();
        Random r = new Random();
        boolean placed = false;

        while (!placed) {
            boolean horiz = r.nextBoolean();
            int x = r.nextInt(10);
            int y = r.nextInt(10);

            if (onlineMode) {
                try {
                    BoostEvent cmd = new BoostEvent(
                            BoostEvent.Kind.TROLL_PLACE,
                            x, y,
                            2,
                            horiz
                    );

                    if (netServer != null) netServer.send(cmd);
                    if (netClient != null) netClient.send(cmd);

                } catch (Exception ignored) {}
            }

            var troll = board.placeFakeShip(2, x, y, horiz);
            if (troll != null) placed = true;
        }

        markBoostUsed(current, BoostType.TROLL);
        addHistoryEntry(current.getName() + " : ustawia fałszywy statek");

        if (p1PlacementView != null) p1PlacementView.update();
        if (p2PlacementView != null) p2PlacementView.update();
    }

    // ===========================
    //   RESZTA LOGIKI
    // ===========================

    private void updateTargetViews() {
        if (p1TargetView != null) p1TargetView.update();
        if (p2TargetView != null) p2TargetView.update();
    }

    private void highlightWholeShip(BoardView view, Ship ship) {

        view.update();
        var board = placingPlayer.getBoard();

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {

                var cell = board.getCell(x, y);
                if (cell.getShip() != ship) continue;

                int gx = x + 1;
                int gy = y + 1;

                for (Node n : view.getGridPane().getChildren()) {

                    Integer cx = GridPane.getColumnIndex(n);
                    Integer cy = GridPane.getRowIndex(n);
                    if (cx == null || cy == null) continue;
                    if (!(n instanceof Rectangle r)) continue;

                    if (cx == gx && cy == gy) {
                        r.getStyleClass().removeAll(
                                "cell-water",
                                "cell-ship",
                                "cell-hit",
                                "cell-miss",
                                "cell-preview-ok",
                                "cell-preview-bad"
                        );
                        r.getStyleClass().add("cell-preview-bad");
                    }
                }
            }
        }
    }

    // ===========================
    //     BOT
    // ===========================

    private void botMove() {

        Player current = engine.getState().getCurrentPlayer();
        Player human = engine.getState().getOtherPlayer();

        if (!(current instanceof BotPlayer bot)) return;

        PauseTransition pause = new PauseTransition(Duration.seconds(1.1));
        pause.setOnFinished(ev -> {

            int[] shot = bot.getAI().chooseShot(human.getBoard());
            int x = shot[0];
            int y = shot[1];

            boolean hit = engine.shoot(x, y);

            updateTargetViews();

            String coord = "" + (char) ('A' + y) + (x + 1);

            if (hit) {

                addHistoryEntry(bot.getName() + ": " + coord + " – Trafienie");

                var cell = human.getBoard().getCell(x, y);
                if (cell.hasShip() && cell.getShip().isSunk())
                    addHistoryEntry("Zatopiony: " + cell.getShip().getName());

                bot.getAI().notifyHit(x, y);

                if (engine.isGameFinished()) {
                    showEndScreen();
                    return;
                }

                botMove();
                return;
            }

            addHistoryEntry(bot.getName() + ": " + coord + " – Pudło");

            if (engine.isGameFinished()) {
                showEndScreen();
                return;
            }

            engine.getState().swapTurn();
            inputLocked = false;
            showBattleScreen();

        });

        pause.play();
    }

    // ===========================
    //     OVERLAY TURY
    // ===========================

    private void showTurnOverlay(Player nextPlayer) {
        // W grze z botem NIE MA overlay tury.
        if (engine.getState().getCurrentPlayer() instanceof BotPlayer
                || engine.getState().getOtherPlayer() instanceof BotPlayer) {

            inputLocked = false;
            showBattleScreen();
            return;
        }

        // --- oryginalny overlay dla gry dwuosobowej ---
        inputLocked = true;
        int SECONDS = 5;

        PauseTransition showResultPause = new PauseTransition(Duration.seconds(2));

        showResultPause.setOnFinished(event -> {

            int[] timeLeft = {SECONDS};

            StackPane overlay = new StackPane();
            overlay.getStyleClass().add("turn-overlay");

            root.setBottom(null);

            VBox box = new VBox(25);
            box.setAlignment(Pos.CENTER);

            Text turnInfo = new Text("Tura gracza: " + nextPlayer.getName());
            turnInfo.getStyleClass().add("turn-info");

            Text countdown = new Text(String.valueOf(timeLeft[0]));
            countdown.getStyleClass().add("turn-countdown");

            box.getChildren().addAll(turnInfo, countdown);
            overlay.getChildren().add(box);

            root.setCenter(overlay);
            root.setRight(null);

            PauseTransition tick = new PauseTransition(Duration.seconds(1));
            tick.setOnFinished(ev -> {
                timeLeft[0]--;
                if (timeLeft[0] <= 0) {
                    inputLocked = false;
                    showBattleScreen();
                } else {
                    countdown.setText(String.valueOf(timeLeft[0]));
                    tick.play();
                }
            });

            tick.play();
        });

        showResultPause.play();
    }

    // ===========================
    //   KONIEC GRY + EXPORT
    // ===========================

    private void showEndScreen() {

        Text endText = new Text("KONIEC!\nWygrał: " + engine.getWinner().getName());
        endText.getStyleClass().add("game-end-text");

        Button menuBtn = new Button("Powrót do menu");
        menuBtn.setPrefWidth(250);
        menuBtn.setOnAction(event -> {
            MenuView menu = new MenuView(stage);
            stage.getScene().setRoot(menu.getRoot());
        });

        Button exportBtn = new Button("Eksportuj wyniki");
        exportBtn.setPrefWidth(250);
        exportBtn.setOnAction(event -> {
            try {
                history.exportToFile(engine.getWinner().getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Button exitBtn = new Button("Wyjdź z gry");
        exitBtn.setPrefWidth(250);
        exitBtn.setOnAction(event -> stage.close());

        VBox buttons = new VBox(10, menuBtn, exportBtn, exitBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox wrapper = new VBox(25, endText, buttons);
        wrapper.setAlignment(Pos.CENTER);

        root.setRight(null);
        root.setCenter(wrapper);
    }




    public void onBoost(BoostEvent cmd) {

        Platform.runLater(() -> {

            Player me = engine.getState().getCurrentPlayer();
            Player enemy = engine.getState().getOtherPlayer();

            switch (cmd.kind) {

                case RADAR -> {
                    addHistoryEntry("Rywal użył RADARU.");
                }

                case SWAP_SELECT -> {
                    addHistoryEntry("Rywal wybrał statek do przeniesienia.");
                }

                case SWAP_MOVE -> {
                    addHistoryEntry("Rywal przeniósł statek.");

                    Board enemyBoard = enemy.getBoard();
                    Ship toMove = null;

                    outer:
                    for (int y = 0; y < 10; y++) {
                        for (int x = 0; x < 10; x++) {
                            Cell c = enemyBoard.getCell(x, y);
                            if (c.hasShip()) {
                                Ship s = c.getShip();
                                if (!s.isFake() && s.getSize() == cmd.size) {
                                    toMove = s;
                                    break outer;
                                }
                            }
                        }
                    }

                    if (toMove != null) {
                        enemyBoard.moveShip(toMove, cmd.x, cmd.y, cmd.horizontal);
                    }

                    updateTargetViews();
                }

                case TROLL_PLACE -> {
                    addHistoryEntry("Rywal ustawił fałszywy statek (TROLL).");
                    enemy.getBoard().placeFakeShip(cmd.size, cmd.x, cmd.y, cmd.horizontal);
                    updateTargetViews();
                }
            }
        });
    }
}
