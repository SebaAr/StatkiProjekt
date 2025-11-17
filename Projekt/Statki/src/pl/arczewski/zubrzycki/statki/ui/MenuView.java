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

        Button localBtn = new Button("Gra lokalna");
        localBtn.setOnAction(e -> {
            PlayerSetupView setup = new PlayerSetupView(stage);
            stage.getScene().setRoot(setup.getRoot());
        });

        Button multiBtn = new Button("Multiplayer (w budowie)");
        multiBtn.setDisable(true);

        root.getChildren().addAll(localBtn, multiBtn);
    }

    public Parent getRoot() { return root; }
}