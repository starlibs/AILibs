package jaicore.graphvisualizer.gui;

import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

public class Tester extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXCode code = new FXCode();
        code.open();
        code.getTabPane().getTabs().add(new Tab("Test",new Button("test")));


    }
}
