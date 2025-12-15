package pl.arczewski.zubrzycki.statki.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MenuView {

    private final VBox root = new VBox(20);

    public MenuView(Stage stage) {
        root.setAlignment(Pos.CENTER);

        // --- GRA LOKALNA ---
        Button localBtn = new Button("Gra lokalna");
        localBtn.setPrefWidth(200);
        localBtn.setPrefHeight(45);
        localBtn.setOnAction(e -> {
            PlayerSetupView setup = new PlayerSetupView(stage);
            stage.getScene().setRoot(setup.getRoot());
        });

        // --- GRA Z BOTEM ---
        Button vsBotBtn = new Button("Gra z botem");
        vsBotBtn.setPrefWidth(200);
        vsBotBtn.setPrefHeight(45);
        vsBotBtn.setOnAction(e -> {
            BotDifficultyView bot = new BotDifficultyView(stage);
            stage.getScene().setRoot(bot.getRoot());
        });

        // --- MULTIPLAYER ---
        Button multiBtn = new Button("Multiplayer");
        multiBtn.setPrefWidth(200);
        multiBtn.setPrefHeight(45);
        multiBtn.setOnAction(e -> {
            MultiplayerView mp = new MultiplayerView(stage);
            stage.getScene().setRoot(mp.getRoot());
        });

        root.getChildren().addAll(localBtn, vsBotBtn, multiBtn);
    }

    public Parent getRoot() { return root; }
}
