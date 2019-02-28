package jaicore.graphvisualizer.plugin.graphview;

import org.graphstream.graph.Node;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;

public class GraphMouseListener implements ViewerListener, Runnable {

	private boolean active;

	private GraphViewPluginModel viewModel;
	private ViewerPipe viewerPipe;

	public GraphMouseListener(GraphViewPluginModel viewModel, ViewerPipe viewerPipe) {
		this.viewModel = viewModel;
		this.viewerPipe = viewerPipe;
		this.active = true;
	}

	@Override
	public void buttonPushed(String id) {
		Node viewGraphNode = viewModel.getGraph().getNode(id);
		Object searchGraphNode = viewModel.getSearchGraphNodeMappedToViewGraphNode(viewGraphNode);
		DefaultGUIEventBus.getInstance().postEvent(new NodeClickedEvent(viewGraphNode, searchGraphNode));
	}

	@Override
	public void buttonReleased(String id) {
		// nothing to do here
	}

	@Override
	public void mouseLeft(String id) {
		// nothing to do here
	}

	@Override
	public void mouseOver(String id) {
		// nothing to do here
	}

	@Override
	public void viewClosed(String id) {
		active = false;
	}

	@Override
	public void run() {
		while (active) {
			viewerPipe.pump();
		}
	}

}
