package pl.arczewski.zubrzycki.statki.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MenuView menu = new MenuView(primaryStage);
        Scene scene = new Scene(menu.getRoot(), 900, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Statki - Gra");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
