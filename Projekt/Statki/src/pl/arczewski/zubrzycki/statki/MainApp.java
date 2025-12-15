package pl.arczewski.zubrzycki.statki.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


import java.util.Objects;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {

        Image bgImage = new Image(
                Objects.requireNonNull(
                        getClass().getResource("../images/background.jpg"),
                        "Nie znaleziono obrazu: ../images/background.jpg"
                ).toExternalForm()
        );

        ImageView bg = new ImageView(bgImage);
        bg.setPreserveRatio(false);
        bg.setSmooth(true);

        bg.setFitWidth(1920);
        bg.setFitHeight(1080);

        bg.setEffect(new GaussianBlur(18));

        StackPane root = new StackPane();

        MenuView menu = new MenuView(primaryStage);

        root.getChildren().addAll(bg, menu.getRoot());
        StackPane.setAlignment(menu.getRoot(), javafx.geometry.Pos.CENTER);
        StackPane.setMargin(menu.getRoot(), new Insets(10));

        Scene scene = new Scene(root, 1300, 750);

        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource("style.css")
                ).toExternalForm()
        );

        primaryStage.setScene(scene);
        primaryStage.setTitle("Statki");
        primaryStage.setMaximized(true);
        primaryStage.show();

        root.widthProperty().addListener((o, oldV, newV) -> bg.setFitWidth(newV.doubleValue()));
        root.heightProperty().addListener((o, oldV, newV) -> bg.setFitHeight(newV.doubleValue()));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
