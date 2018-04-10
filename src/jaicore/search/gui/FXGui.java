package jaicore.search.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FXGui extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("gui.fxml"));

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Gui");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String [] args){
        launch(args);
    }
}
