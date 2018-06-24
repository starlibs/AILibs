package jaicore.graphvisualizer.gui;

import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;
import javafx.application.Platform;

public class VisualizationWindow<T> {
	
	Thread fxThread;
	Recorder recorder;

	
	public VisualizationWindow(IObservableGraphAlgorithm observable, String title) {
		if(fxThread == null) {
			try {

				fxThread = new Thread() {
					@Override
					public void run() {
						javafx.application.Application.launch(GuiApplication.class);
					}
				};
				fxThread.start();
			}
			catch(IllegalStateException e){

			}

		}
		try {
			recorder = new Recorder<>(observable);
			Platform.runLater(()->{
				GuiApplication app = new GuiApplication();

					new FXGui().open(recorder,title);

			});

		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}

	public void addDataSupplier(ISupplier supplier){
		recorder.addDataSupplier(supplier);
	}


}
