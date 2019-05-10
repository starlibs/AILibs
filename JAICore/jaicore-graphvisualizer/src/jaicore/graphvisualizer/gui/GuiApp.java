package jaicore.graphvisualizer.gui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The GuiApp is used to create the fx-Thread for the Visualization
 * 
 */
public class GuiApp extends Application {


	@Override
	public void start(Stage stage) throws Exception {
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		System.exit(0);
	}
}
