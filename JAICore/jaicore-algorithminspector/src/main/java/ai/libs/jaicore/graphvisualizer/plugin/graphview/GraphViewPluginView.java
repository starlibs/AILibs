package ai.libs.jaicore.graphvisualizer.plugin.graphview;

import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer.ThreadingModel;
import org.graphstream.ui.view.ViewerPipe;

import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import javafx.scene.layout.BorderPane;

public class GraphViewPluginView extends ASimpleMVCPluginView<GraphViewPluginModel, GraphViewPluginController, BorderPane> {

	private FxViewer fxViewer;
	private Thread listenerThread;

	public GraphViewPluginView(final GraphViewPluginModel model) {
		super(model, new BorderPane());
		this.fxViewer = new FxViewer(model.getGraph(), ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		this.fxViewer.enableAutoLayout();

		FxViewPanel fxViewPanel = (FxViewPanel) this.fxViewer.addDefaultView(false);
		this.getNode().setCenter(fxViewPanel);

		this.initializeGraphMouseListener();
	}

	private void initializeGraphMouseListener() {
		ViewerPipe viewerPipe = this.fxViewer.newViewerPipe();
		GraphMouseListener graphMouseListener = new GraphMouseListener(this.getModel(), viewerPipe);
		viewerPipe.addViewerListener(graphMouseListener);
		viewerPipe.addSink(this.getModel().getGraph());

		this.listenerThread = new Thread(graphMouseListener, "Graph View Plugin");
		this.listenerThread.setDaemon(true);
		this.listenerThread.start();
	}

	@Override
	public void update() {
		// No need for code here as the update happens automatically via the graphstream graph
	}

	public void stop() {
		this.listenerThread.interrupt();
		this.fxViewer.close();
	}

	@Override
	public void clear() {
		// No need for code here as the update happens automatically via the graphstream graph
	}
}