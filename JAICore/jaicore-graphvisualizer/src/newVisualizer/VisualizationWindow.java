package newVisualizer;

import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.gui.Recorder;
import javafx.application.Platform;

public class VisualizationWindow<T> {
	
	Thread fxThread;
	
	public VisualizationWindow(IObservableGraphAlgorithm observable) {
		System.out.println("Test");
		
		if(fxThread == null) {
			fxThread = new Thread() {
				@Override
				public void run() {
					javafx.application.Application.launch(GuiApp.class);
				}
			};
			fxThread.start();
			System.out.println("thread startet");
		}
		try {
			System.out.println("Test2");
			Recorder recorder = new Recorder<>(observable);
			Platform.runLater(()->{
				GuiApp app = new GuiApp();

					app.open("Test", recorder);

			});
			
			
			System.out.println("test3");
			
			
			
			
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}

}
