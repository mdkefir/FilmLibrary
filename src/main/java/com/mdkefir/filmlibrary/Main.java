package com.mdkefir.filmlibrary;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setMinWidth(925);
            primaryStage.setMinHeight(620);
            Button closeButton = new Button("Закрыть");
            closeButton.setOnAction(e -> primaryStage.close());
            Parent root = FXMLLoader.load(getClass().getResource("/com/mdkefir/filmlibrary/fxml/main.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("FilmLibrary");
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

