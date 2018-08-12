package jaicore.graphvisualizer.guiOld;

import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.guiOld.dataSupplier.ISupplier;
import javafx.application.Platform;

/**
 * Class which creates a Thread and a VisualizationWindow. For this the algorithm and a title are needed.
 * @author jkoepe
 *
 * @param <T>
 */

public class VisualizationWindow<T> {
	/**
	 * The Javafx-thread which contains the GUI
	 */
	Thread fxThread;
	
	/**
	 * A recorder which is connected to the algorithm
	 */
	Recorder recorder;

	
	/**
	 * The construction of a new VisualizationWindow. 
	 * 
	 * @param observable
	 * 		The algorithm which should be observed
	 * @param title
	 * 		The title of the window
	 */
	public VisualizationWindow(IObservableGraphAlgorithm observable, String title) {
		//if there is no fxThread, create a new one and start it
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
		
		//try to create a recorder and start the gui in the fxthread.
		//if it fails to create the recorder the system is exited.
		try {
			recorder = new Recorder<>(observable);
			Platform.runLater(()->{
				GuiApplication app = new GuiApplication();

					new FXGui().open(recorder,title);
					Boolean finish = new FXCode().open(recorder);

			});

		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}

	/**
	 * Adds a datasupplier to the recorder.
	 * @param supplier
	 * 		The added supplier.
	 */
	public void addDataSupplier(ISupplier supplier){
		recorder.addDataSupplier(supplier);
	}


}
