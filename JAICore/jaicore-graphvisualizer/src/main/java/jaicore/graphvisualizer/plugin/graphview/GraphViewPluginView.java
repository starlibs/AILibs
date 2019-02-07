package jaicore.graphvisualizer.plugin.graphview;

import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer.ThreadingModel;
import org.graphstream.ui.view.ViewerPipe;

import jaicore.graphvisualizer.plugin.GUIPluginView;
import javafx.scene.Node;

public class GraphViewPluginView implements GUIPluginView {

	private GraphViewPluginModel model;

	private FxViewer fxViewer;

	public GraphViewPluginView() {
		this.model = new GraphViewPluginModel(this);
		this.fxViewer = new FxViewer(model.getGraph(), ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		this.fxViewer.enableAutoLayout();

		initializeGraphMouseListener();
	}

	private void initializeGraphMouseListener() {
		ViewerPipe viewerPipe = fxViewer.newViewerPipe();
		GraphMouseListener graphMouseListener = new GraphMouseListener(model, viewerPipe);
		viewerPipe.addViewerListener(graphMouseListener);
		viewerPipe.addSink(model.getGraph());

		Thread listenerThread = new Thread(graphMouseListener);
		listenerThread.start();
	}

	@Override
	public Node getNode() {
		return (FxViewPanel) fxViewer.addDefaultView(false);
	}

	@Override
	public void update() {
		// No need for code here as the update happens automatically via the graphstream graph

	}

	@Override
	public String getTitle() {
		return "Search Graph Viewer";
	}

	public GraphViewPluginModel getModel() {
		return model;
	}

}