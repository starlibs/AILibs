package jaicore.graphvisualizer.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class GuiApplication extends Application {
    FXGui gui;


    @Override
    public void start(Stage stage) throws Exception {
        this.gui = new FXGui();
    }


	@Override
	public void stop() throws Exception {
	
		super.stop();
		System.exit(0);
	}
    
    

}
