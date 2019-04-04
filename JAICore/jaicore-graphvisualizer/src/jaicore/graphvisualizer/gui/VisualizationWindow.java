package jaicore.graphvisualizer.gui;

import jaicore.graph.IGraphAlgorithm;
import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;
import jaicore.graphvisualizer.gui.dataSupplier.TooltipSupplier;
import javafx.application.Platform;

/**
 * Class which creates a thread and a VisualizationWindow.
 */
public class VisualizationWindow<N, E> {
	/**
	 * The Javafx-thread which contains the GUI
	 */
	static Thread fxThread;

	/**
	 * A recorder which is connected to the algorithm
	 */
	Recorder recorder;

	private TooltipSupplier tooltipSupplier;

	public VisualizationWindow(IGraphAlgorithm graphAlgorithm) {
		this(graphAlgorithm, "Visualizer for " + graphAlgorithm, null);
	}

	public VisualizationWindow(IGraphAlgorithm graphAlgorithm, String title) {
		this(graphAlgorithm, title, null);
	}
	/**
	 * The construction of a new VisualizationWindow.
	 *
	 * @param observable The algorithm which should be observed
	 * @param title      The title of the window
	 */
	public VisualizationWindow(IGraphAlgorithm<?, ?, N, E> observable, String title, ObjectEvaluator eval) {
		this.tooltipSupplier = new TooltipSupplier();
		this.tooltipSupplier.setGenerator(getTooltipGenerator());
		if (fxThread == null) {
			try {
				fxThread = new Thread() {
					@Override
					public void run() {
						javafx.application.Application.launch(GuiApp.class);
					}
				};
				fxThread.start();
			} catch (IllegalStateException e) {

			}
		}

		/* try to create a recorder and start the gui in the fxthread.
		 if it fails to create the recorder the system is exited.*/
		try {
			recorder = new Recorder(observable);
			Thread.sleep(500);
			Platform.runLater(() -> {
				FXCode code = new FXCode(recorder, title, eval);
			});

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		this.addDataSupplier(tooltipSupplier);
	}

	public void addDataSupplier(ISupplier supplier) {
		recorder.addDataSupplier(supplier);
	}

	private TooltipGenerator<N> getTooltipGenerator() {
		return new TooltipGenerator<N>() {
			@Override
			public String getTooltip(N node) {
				return node.toString();
			}
		};
	}

	public void setTooltipGenerator(TooltipGenerator<?> generator) {
		this.tooltipSupplier.setGenerator(generator);
	}

}
