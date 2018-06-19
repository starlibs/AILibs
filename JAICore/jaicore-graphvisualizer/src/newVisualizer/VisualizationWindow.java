package newVisualizer;

import jaicore.graph.observation.IObservableGraphAlgorithm;
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
			
			Platform.runLater(()->{
				GuiApp app = new GuiApp();
				System.out.println("Hier bin ich ");
				for (int i = 0; i <3 ; i ++) {
					app.open(i);
				}
			});
			
			
			System.out.println("test3");
			
			
			
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
