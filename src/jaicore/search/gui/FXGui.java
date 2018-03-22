package jaicore.search.gui;

import com.google.common.eventbus.EventBus;
import jaicore.graphvisualizer.SearchVisualizationPanel;
import jaicore.search.structure.core.GraphEventBus;
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
        FXController.setRec(new Recorder());
        launch(args);
    }
}
