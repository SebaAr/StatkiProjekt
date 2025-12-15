package pl.arczewski.zubrzycki.statki.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pl.arczewski.zubrzycki.statki.engine.GameEngine;
import pl.arczewski.zubrzycki.statki.model.Player;

public class PlayerSetupView {

    private final VBox root = new VBox(15);

    public PlayerSetupView(Stage stage) {
        root.setAlignment(Pos.CENTER);
        root.setFillWidth(false);

        TextField player1Field = new TextField();
        player1Field.setPromptText("Nazwa gracza 1");
        player1Field.setPrefWidth(300);
        player1Field.setPrefHeight(45);

        TextField player2Field = new TextField();
        player2Field.setPromptText("Nazwa gracza 2");
        player2Field.setPrefWidth(300);
        player2Field.setPrefHeight(45);

        // --- BOOSTY WŁĄCZONE / WYŁĄCZONE ---
        CheckBox boostsCheck = new CheckBox("Włącz boosty");
        boostsCheck.setSelected(true); // domyślnie włączone

        Button startBtn = new Button("Rozpocznij ustawianie statków");
        startBtn.setPrefWidth(300);
        startBtn.setPrefHeight(45);

        Button backBtn = new Button("Powrót do menu");
        backBtn.setPrefWidth(300);
        backBtn.setPrefHeight(45);

        startBtn.setOnAction(e -> {
            Player p1 = new Player(player1Field.getText().isEmpty() ? "Gracz 1" : player1Field.getText());
            Player p2 = new Player(player2Field.getText().isEmpty() ? "Gracz 2" : player2Field.getText());

            GameEngine engine = new GameEngine(p1, p2);

            // ustawienie flagi boostów
            engine.getState().setBoostsEnabled(boostsCheck.isSelected());

            GameView gameView = new GameView(stage, engine);
            stage.getScene().setRoot(gameView.getRoot());
        });

        backBtn.setOnAction(e -> {
            MenuView menu = new MenuView(stage);
            stage.getScene().setRoot(menu.getRoot());
        });

        root.getChildren().addAll(
                player1Field,
                player2Field,
                boostsCheck,
                startBtn,
                backBtn
        );
    }

    public Parent getRoot() { return root; }
}
