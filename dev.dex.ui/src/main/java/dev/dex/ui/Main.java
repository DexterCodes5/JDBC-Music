package dev.dex.ui;

import dev.dex.db.*;
import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.stage.*;

import java.io.*;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        Controller controller = fxmlLoader.getController();
        controller.listArtists();
        stage.setTitle("Music Database");
        stage.setScene(scene);
        stage.show();


    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void init() throws Exception {
        if (!Datasource.getInstance().open()) {
            System.out.println("Fatal Error: Can't connect to database, closing application.");
            Platform.exit();
        }
    }

    @Override
    public void stop() throws Exception {
        Datasource.getInstance().close();
    }
}