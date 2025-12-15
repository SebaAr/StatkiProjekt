package pl.arczewski.zubrzycki.statki.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pl.arczewski.zubrzycki.statki.engine.GameEngine;
import pl.arczewski.zubrzycki.statki.model.Player;
import pl.arczewski.zubrzycki.statki.network.GameClient;
import pl.arczewski.zubrzycki.statki.network.GameServer;
import pl.arczewski.zubrzycki.statki.network.LobbyClient;
import pl.arczewski.zubrzycki.statki.network.LobbyServer;

import java.io.IOException;

public class MultiplayerView {

    private final BorderPane root = new BorderPane();
    private final Stage stage;

    private static final int DEFAULT_LOBBY_PORT = 9000;
    private static final int DEFAULT_GAME_PORT = 5000;

    public MultiplayerView(Stage stage) {
        this.stage = stage;
        root.setPadding(new Insets(20));

        Text title = new Text("Multiplayer – połączenie online");
        title.getStyleClass().add("player-name");

        VBox main = new VBox(20);
        main.setAlignment(Pos.TOP_CENTER);
        main.getChildren().add(title);

        HBox panels = new HBox(30);
        panels.setAlignment(Pos.TOP_CENTER);
        panels.getChildren().addAll(
                createHostPane(),
                createJoinPane()
        );
        main.getChildren().add(panels);

        Button backBtn = new Button("Powrót do menu");
        backBtn.setPrefWidth(220);
        backBtn.setOnAction(e -> {
            MenuView menu = new MenuView(stage);
            stage.getScene().setRoot(menu.getRoot());
        });

        VBox bottom = new VBox(15, backBtn);
        bottom.setAlignment(Pos.CENTER);

        root.setCenter(main);
        root.setBottom(bottom);
    }

    public Parent getRoot() {
        return root;
    }

    // ===========================
    // PANEL HOSTA
    // ===========================
    private Pane createHostPane() {

        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setAlignment(Pos.TOP_LEFT);
        box.setPrefWidth(350);
        box.getStyleClass().add("pane-background");

        Label section = new Label("Utwórz pokój (HOST)");
        section.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField nameField = new TextField("Gracz 1");
        TextField lobbyPortField = new TextField(String.valueOf(DEFAULT_LOBBY_PORT));
        TextField gamePortField = new TextField(String.valueOf(DEFAULT_GAME_PORT));

        Label roomCodeLabel = new Label("Kod pokoju: (jeszcze nie utworzono)");
        Label statusLabel = new Label();

        Button createBtn = new Button("Utwórz pokój");
        createBtn.setPrefWidth(180);

        Button startGameBtn = new Button("Rozpocznij grę");
        startGameBtn.setPrefWidth(180);
        startGameBtn.setDisable(true);

        createBtn.setOnAction(e -> {

            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) playerName = "Gracz 1";

            int tmpLobbyPort;
            int tmpGamePort;

            try {
                tmpLobbyPort = Integer.parseInt(lobbyPortField.getText().trim());
                tmpGamePort = Integer.parseInt(gamePortField.getText().trim());
            } catch (NumberFormatException ex) {
                statusLabel.setText("Błędny numer portu.");
                return;
            }

            final int lobbyPort = tmpLobbyPort;
            final int gamePort = tmpGamePort;
            final String fPlayerName = playerName;

            createBtn.setDisable(true);
            statusLabel.setText("Uruchamianie serwera lobby i gry...");

            // 1) LobbyServer – wbudowany serwer pokoju na HOST-cie
            LobbyServer lobbyServer = new LobbyServer(lobbyPort);
            lobbyServer.startAsync();

            // 2) GameServer – serwer właściwej gry
            GameServer gameServer = new GameServer();
            Thread gameServerThread = new Thread(() -> {
                try {
                    System.out.println("[GameServer] start na porcie " + gamePort);
                    gameServer.start(gamePort);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }, "GameServer-Main");
            gameServerThread.setDaemon(true);
            gameServerThread.start();

            statusLabel.setText("Tworzenie pokoju w lobby...");

            // 3) Host łączy się do WŁASNEGO lobby (localhost)
            new Thread(() -> {
                try {
                    // mała pauza, żeby LobbyServer na pewno już nasłuchiwał
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {}

                    LobbyClient lobbyClient = new LobbyClient("localhost", lobbyPort);
                    LobbyClient.CreateRoomResult res = lobbyClient.createRoom(gamePort);

                    String code = res.roomCode();

                    Platform.runLater(() -> {
                        roomCodeLabel.setText("Kod pokoju: " + code);
                        statusLabel.setText("Pokój utworzony. Przekaż kod drugiemu graczowi.");
                        startGameBtn.setDisable(false);

                        // zapamiętujemy kontekst hosta
                        startGameBtn.setUserData(new HostContext(fPlayerName, gameServer, lobbyServer));
                    });

                } catch (IOException ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        statusLabel.setText("Błąd lobby: " + ex.getMessage());
                        createBtn.setDisable(false);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        statusLabel.setText("Błąd: " + ex.getMessage());
                        createBtn.setDisable(false);
                    });
                }
            }, "LobbyCreateRoom").start();
        });

        startGameBtn.setOnAction(e -> {
            HostContext ctx = (HostContext) startGameBtn.getUserData();

            Player p1 = new Player(ctx.playerName());
            Player p2 = new Player("Gracz online");

            GameEngine engine = new GameEngine(p1, p2);

            // jesteś HOSTEM – przekazujemy GameServer
            GameView view = new GameView(stage, engine, ctx.gameServer(), null);
            stage.getScene().setRoot(view.getRoot());
        });

        box.getChildren().addAll(
                section,
                new Label("Nick gracza:"), nameField,
                new Label("Port lobby:"), lobbyPortField,
                new Label("Port gry:"), gamePortField,
                createBtn,
                roomCodeLabel,
                statusLabel,
                startGameBtn
        );

        return box;
    }

    // ===========================
    // PANEL GOŚCIA
    // ===========================
    private Pane createJoinPane() {

        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setAlignment(Pos.TOP_LEFT);
        box.setPrefWidth(350);
        box.getStyleClass().add("pane-background");

        Label section = new Label("Dołącz do pokoju (GOŚĆ)");
        section.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField nameField = new TextField("Gracz 2");

        // tu gość wpisuje IP HOSTA!
        TextField lobbyHostField = new TextField();
        lobbyHostField.setPromptText("np. 111.000.0.000");

        TextField lobbyPortField = new TextField(String.valueOf(DEFAULT_LOBBY_PORT));
        TextField roomCodeField = new TextField();

        Label statusLabel = new Label();

        Button joinBtn = new Button("Dołącz");
        joinBtn.setPrefWidth(180);

        joinBtn.setOnAction(e -> {

            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) playerName = "Gracz 2";

            String lobbyHost = lobbyHostField.getText().trim();
            if (lobbyHost.isEmpty()) {
                statusLabel.setText("Podaj adres IP hosta (serwera lobby).");
                return;
            }

            int lobbyPort;
            try {
                lobbyPort = Integer.parseInt(lobbyPortField.getText().trim());
            } catch (NumberFormatException ex) {
                statusLabel.setText("Błędny port lobby.");
                return;
            }

            String code = roomCodeField.getText().trim().toUpperCase();
            if (code.isEmpty()) {
                statusLabel.setText("Podaj kod pokoju.");
                return;
            }

            final String fPlayerName = playerName;
            final String fLobbyHost = lobbyHost;
            final int fLobbyPort = lobbyPort;
            final String fCode = code;

            joinBtn.setDisable(true);
            statusLabel.setText("Łączenie z lobby...");

            new Thread(() -> {
                try {
                    LobbyClient lobbyClient = new LobbyClient(fLobbyHost, fLobbyPort);
                    LobbyClient.JoinRoomResult res = lobbyClient.joinRoom(fCode);

                    String hostIp = res.hostIp();
                    int gamePort = res.hostGamePort();

                    GameClient client = new GameClient();
                    client.connect(hostIp, gamePort);

                    Platform.runLater(() -> {
                        statusLabel.setText("Połączono z hostem.");

                        Player p1 = new Player("Gracz online");
                        Player p2 = new Player(fPlayerName);

                        GameEngine engine = new GameEngine(p1, p2);

                        // jesteś GOŚCIEM – przekazujemy GameClient
                        GameView view = new GameView(stage, engine, null, client);
                        stage.getScene().setRoot(view.getRoot());
                    });

                } catch (IOException ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        statusLabel.setText("Błąd: " + ex.getMessage());
                        joinBtn.setDisable(false);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        statusLabel.setText("Nie można połączyć z hostem.");
                        joinBtn.setDisable(false);
                    });
                }
            }, "LobbyJoinRoom").start();
        });

        box.getChildren().addAll(
                section,
                new Label("Nick gracza:"), nameField,
                new Label("Adres serwera lobby (IP hosta):"), lobbyHostField,
                new Label("Port lobby:"), lobbyPortField,
                new Label("Kod pokoju:"), roomCodeField,
                joinBtn,
                statusLabel
        );

        return box;
    }

    // ===========================
    // KONTEKST HOSTA
    // ===========================
    private record HostContext(String playerName, GameServer gameServer, LobbyServer lobbyServer) {}
}
