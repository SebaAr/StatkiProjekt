package pl.arczewski.zubrzycki.statki.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pl.arczewski.zubrzycki.statki.engine.GameEngine;
import pl.arczewski.zubrzycki.statki.model.Player;

public class PlayerSetupView {

    private final VBox root = new VBox(15);

    public PlayerSetupView(Stage stage) {
        root.setAlignment(Pos.CENTER);

        TextField player1Field = new TextField();
        player1Field.setPromptText("Nazwa gracza 1");

        TextField player2Field = new TextField();
        player2Field.setPromptText("Nazwa gracza 2");

        Button startBtn = new Button("Rozpocznij ustawianie statków");
        startBtn.setOnAction(e -> {
            Player p1 = new Player(player1Field.getText().isEmpty() ? "Gracz 1" : player1Field.getText());
            Player p2 = new Player(player2Field.getText().isEmpty() ? "Gracz 2" : player2Field.getText());

            GameEngine engine = new GameEngine(p1, p2);
            GameView gameView = new GameView(stage, engine);
            stage.getScene().setRoot(gameView.getRoot());
        });

        root.getChildren().addAll(player1Field, player2Field, startBtn);
    }

    public Parent getRoot() { return root; }
}
