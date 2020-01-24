package ai.libs.jaicore.graphvisualizer.plugin.graphview;

import org.graphstream.graph.Node;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import ai.libs.jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;

public class GraphMouseListener implements ViewerListener, Runnable {

	private boolean active;

	private GraphViewPluginModel viewModel;
	private ViewerPipe viewerPipe;

	public GraphMouseListener(final GraphViewPluginModel viewModel, final ViewerPipe viewerPipe) {
		this.viewModel = viewModel;
		this.viewerPipe = viewerPipe;
		this.active = true;
	}

	@Override
	public void buttonPushed(final String id) {
		Node viewGraphNode = this.viewModel.getGraph().getNode(id);
		String searchGraphNode = this.viewModel.getSearchGraphNodeMappedToViewGraphNode(viewGraphNode);
		DefaultGUIEventBus.getInstance().postEvent(new NodeClickedEvent(viewGraphNode, searchGraphNode));
	}

	@Override
	public void buttonReleased(final String id) {
		// nothing to do here
	}

	@Override
	public void mouseLeft(final String id) {
		// nothing to do here
	}

	@Override
	public void mouseOver(final String id) {
		// nothing to do here
	}

	@Override
	public void viewClosed(final String id) {
		// this.active = false; // This is not good here. This directly closes the listener and thus the pump thread.
	}

	@Override
	public void run() {
		while (this.active) {
			if (Thread.currentThread().isInterrupted()) {
				this.active = false;
				return;
			}
			this.viewerPipe.pump();
		}
	}

}
