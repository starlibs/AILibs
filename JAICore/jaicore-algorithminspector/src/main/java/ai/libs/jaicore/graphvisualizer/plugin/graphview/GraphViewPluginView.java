package ai.libs.jaicore.graphvisualizer.plugin.graphview;

import org.api4.java.common.control.ILoggingCustomizable;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer.ThreadingModel;
import org.graphstream.ui.view.ViewerPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginView;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public class GraphViewPluginView implements IGUIPluginView, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(GraphViewPluginView.class);
	private GraphViewPluginModel model;

	private FxViewer fxViewer;
	private BorderPane graphParentLayout;
	private Thread listenerThread;

	public GraphViewPluginView() {
		this.model = new GraphViewPluginModel(this);
		this.fxViewer = new FxViewer(this.model.getGraph(), ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		this.fxViewer.enableAutoLayout();

		this.graphParentLayout = new BorderPane();

		this.initializeGraphMouseListener();
	}

	private void initializeGraphMouseListener() {
		ViewerPipe viewerPipe = this.fxViewer.newViewerPipe();
		GraphMouseListener graphMouseListener = new GraphMouseListener(this.model, viewerPipe);
		viewerPipe.addViewerListener(graphMouseListener);
		viewerPipe.addSink(this.model.getGraph());

		this.listenerThread = new Thread(graphMouseListener, "Graph View Plugin");
		this.listenerThread.setDaemon(true);
		this.listenerThread.start();
	}

	@Override
	public Node getNode() {
		FxViewPanel fxViewPanel = (FxViewPanel) this.fxViewer.addDefaultView(false);
		this.graphParentLayout.setCenter(fxViewPanel);
		return this.graphParentLayout;
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
		return this.model;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	public void stop() {
		this.listenerThread.interrupt();
		this.fxViewer.close();
	}
}