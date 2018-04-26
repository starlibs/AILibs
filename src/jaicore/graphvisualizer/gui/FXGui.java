package jaicore.graphvisualizer.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FXGui extends Application {



    @Override
    public void start(Stage stage) throws Exception {
        //load the fxml file of the gui
        Parent root = FXMLLoader.load(getClass().getResource("gui.fxml"));

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Gui");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Start the gui with an empty recorder.
     * @param args
     */
    public static void main(String [] args){
        FXController.setRec(new Recorder());
        launch(args);
    }
}
