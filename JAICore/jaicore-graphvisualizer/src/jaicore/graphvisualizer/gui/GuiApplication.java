package jaicore.graphvisualizer.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public abstract class GuiApplication extends Application {
    protected FXGui gui;


    @Override
    public void start(Stage stage) throws Exception {
        this.gui = new FXGui();
        startGui();

    }
    public abstract void startGui();
}
