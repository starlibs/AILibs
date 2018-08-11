package jaicore.graphvisualizer;

import com.google.common.eventbus.Subscribe;
import jaicore.graphvisualizer.events.graphEvents.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("serial")
public class SearchVisualizationPanel<T> extends JPanel {

	private TooltipGenerator<T> tooltipGenerator;

	private int nodeCounter = 0;

	private List<T> roots;

	private final Graph graph;
//	private final Viewer viewer;
//	private final View view;
//	private final ViewerPipe viewerPipe;

	private final Container graphContainer = new Container();
	private final JFrame tooltipContainer = new JFrame();
	private final JLabel tooltipLabel = new JLabel();
	private final JScrollPane scrollPane = new JScrollPane(tooltipLabel);
	private Timer tooltipTimer = null;

	private final ConcurrentMap<T, Node> ext2intNodeMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<Node, T> int2extNodeMap = new ConcurrentHashMap<>();

	public SearchVisualizationPanel() {
		super();
		this.roots = new ArrayList<T>();

		/* setup layout for the jPanel */
		setLayout(new OverlayLayout(this));

		/* setup graphstream stuff */
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		this.graph = new SingleGraph("Search Visualizer");
//		this.viewer = new Viewer(this.graph, ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
//		this.viewer.enableAutoLayout();
//		this.view = this.viewer.addDefaultView(false);
//		final Camera cam = this.view.getCamera();
//		cam.setAutoFitView(true);

		/* add containers to canvas */
		tooltipContainer.setLayout(new BorderLayout());
		tooltipContainer.setMinimumSize(new Dimension(100, 100));
		Dimension d = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
		tooltipContainer.setMaximumSize(d);
		tooltipContainer.setSize(d);
		tooltipContainer.getContentPane().add(scrollPane, BorderLayout.CENTER);
		graphContainer.setLayout(new BorderLayout());
//		graphContainer.add((JPanel) this.view, BorderLayout.CENTER);
		add(graphContainer);

		/* add timer for disappearing tooltips */
		this.tooltipTimer = new Timer(20000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//            	SearchVisualizationPanel.this.tooltipContainer.setVisible(false);
            }
        });
		this.tooltipTimer.setRepeats(false);

		/* add listener for mouse events */
//		viewerPipe = this.viewer.newViewerPipe();
//		new PumpThread(viewerPipe, 50).start();

		/* load css file for formatting the graph */
//		graph.addAttribute("ui.stylesheet", "url('conf/searchgraph.css')");

		/* attach a listener */
		this.tooltipGenerator = new TooltipGenerator<T>() {
			@Override
			public String getTooltip(T node) {
				return node.toString();
			}
		};
//		this.viewerPipe.addViewerListener(new ViewerListener() {
//
//			@Override
//			public void viewClosed(String arg0) {
//			}
//
//			// @Override
//			// public void mouseOver(String arg0) {
//			// }
//			//
//			// @Override
//			// public void mouseLeft(String arg0) {
//			// }
//
//			@Override
//			public void buttonReleased(String arg0) {
//			}
//
//			@Override
//			public void buttonPushed(String arg0) {
//
//				Runnable doButtonPushedAction = new Runnable() {
//				    public void run() {
//
//				    	/* generate HTML for tooltip and set it */
//						StringBuilder sb = new StringBuilder();
////						sb.append("<html><div style='padding: 5px; background: #ffffcc; border: 1px solid black;'>");
//						sb.append("<html><div style='padding: 5px;'>");
//						sb.append(SearchVisualizationPanel.this.tooltipGenerator.getTooltip(SearchVisualizationPanel.this.getNodeOfString(arg0)));
//						sb.append("</div></html>");
//						SearchVisualizationPanel.this.tooltipLabel.setText(sb.toString());
//
//						/* determine desired position for tooltip box */
//						final Point mousePosition = MouseInfo.getPointerInfo().getLocation();
//						SwingUtilities.convertPointFromScreen(mousePosition, SearchVisualizationPanel.this);
//						Dimension size;
//						final Dimension preferredSize = SearchVisualizationPanel.this.tooltipLabel.getPreferredSize();
//						if (preferredSize.getWidth() <= getWidth() * 0.5)
//							size = preferredSize;
//						else {
//							javax.swing.text.View view = (javax.swing.text.View) tooltipLabel.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
//							view.setSize((int) Math.round(getWidth() * 0.5), 0);
//							float w = view.getPreferredSpan(javax.swing.text.View.X_AXIS);
//							float h = view.getPreferredSpan(javax.swing.text.View.Y_AXIS);
//							size = new java.awt.Dimension((int) Math.ceil(w), (int) Math.ceil(h));
//						}
//						final Point position = new Point(mousePosition.x + 15, mousePosition.y);
//
//						/* check if the tooltip is partly outside the window on the bottom */
//						final int yOffset = getHeight() - size.height - position.y;
//						if (yOffset < 0) {
//							position.y = position.y + yOffset;
//						}
//
//						/* check if the tooltip is partly outside the window on the right side. */
//						final boolean rightOutside = position.x + size.width > getWidth();
//						if (rightOutside) {
//							position.x = mousePosition.x - 15 - size.width;
//						}
////						SearchVisualizationPanel.this.tooltipLabel.setBounds(position.x, position.y, size.width, size.height);
//
//						SearchVisualizationPanel.this.scrollPane.getViewport().setViewPosition(new Point(0,0));
//						SearchVisualizationPanel.this.scrollPane.revalidate();
//						Dimension d = SearchVisualizationPanel.this.scrollPane.getPreferredSize();
//						d.height += 50;
//						d.width += 50;
//						if ( d.height > (SearchVisualizationPanel.this.tooltipContainer.getMaximumSize().height-50) )
//							d.height = (SearchVisualizationPanel.this.tooltipContainer.getMaximumSize().height-50);
//						if ( d.width > (SearchVisualizationPanel.this.tooltipContainer.getMaximumSize().width-50) )
//							d.width = (SearchVisualizationPanel.this.tooltipContainer.getMaximumSize().width-50);
//						SearchVisualizationPanel.this.tooltipContainer.setSize(d);
//						SearchVisualizationPanel.this.tooltipContainer.repaint();
//		            	SearchVisualizationPanel.this.tooltipContainer.setExtendedState(0);
//
//		            	SearchVisualizationPanel.this.tooltipContainer.setVisible(true);
//		            	SearchVisualizationPanel.this.tooltipTimer.restart();
//					}
//				};
//
//				SwingUtilities.invokeLater(doButtonPushedAction);
//			}
//		});
	}

	protected synchronized Node newNode(final T newNodeExt) {

		/* create new node */
		final String nodeId = "n" + (nodeCounter++);
		if (this.ext2intNodeMap.containsKey(newNodeExt) || this.graph.getNode(nodeId) != null) {
			throw new IllegalArgumentException("Cannot insert node " + newNodeExt + " because it is already known.");
		}
		final Node newNodeInt = this.graph.addNode(nodeId);

		/* store relation between node in graph and internal representation of the node */
		this.ext2intNodeMap.put(newNodeExt, newNodeInt);
		this.int2extNodeMap.put(newNodeInt, newNodeExt);

		/* store relation between node an parent in internal model */
		return newNodeInt;
	}

	protected synchronized Edge newEdge(final T from, final T to) {
		final Node fromInt = this.ext2intNodeMap.get(from);
		final Node toInt = this.ext2intNodeMap.get(to);
		if (fromInt == null)
			throw new IllegalArgumentException("Cannot insert edge between " + from + " and " + to + " since node " + from + " does not exist.");
		if (toInt == null)
			throw new IllegalArgumentException("Cannot insert edge between " + from + " and " + to + " since node " + to + " does not exist.");
		final String edgeId = fromInt.getId() + "-" + toInt.getId();
//TODO		return this.graph.addEdge(edgeId, fromInt, toInt, true);
		return this.graph.addEdge(edgeId, fromInt, toInt, false);
	}

	protected synchronized boolean removeEdge(final T from, final T to) {
//		for(Edge e: graph.getEachEdge()) {
//			if (e.getSourceNode().equals(this.ext2intNodeMap.get(from)) && e.getTargetNode().equals(this.ext2intNodeMap.get(to))) {
//				graph.removeEdge(e);
//				return true;
//			}
//		}
		return false;
	}

	@Subscribe
	public synchronized void receiveGraphInitEvent(GraphInitializedEvent<T> e) {
		try {

//			if (roots != null)
//				throw new UnsupportedOperationException("Cannot initialize the graph for a second time!");
			roots.add(e.getRoot());
			if (roots == null)
				throw new IllegalArgumentException("Root must not be NULL");
			newNode(roots.get(roots.size()-1));
//			ext2intNodeMap.get(roots.get(roots.size()-1)).addAttribute("ui.class", "root");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Subscribe
	public synchronized void receiveNewNodeEvent(NodeReachedEvent<T> e) {
		try {
			if (!ext2intNodeMap.containsKey(e.getNode()))
				newNode(e.getNode());
			newEdge(e.getParent(), e.getNode());
//			ext2intNodeMap.get(e.getNode()).addAttribute("ui.class", e.getType());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Subscribe
	public synchronized void receiveNodeTypeSwitchEvent(NodeTypeSwitchEvent<T> e) {
		try {
			if (roots.contains(e.getNode()))
				return;
			if (!ext2intNodeMap.containsKey(e.getNode()))
				throw new NoSuchElementException("Cannot switch type of node " + e.getNode() + ". This node has not been reached previously.");
//			ext2intNodeMap.get(e.getNode()).addAttribute("ui.class", e.getType());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Subscribe
	public synchronized void receivedNodeParentSwitchEvent(NodeParentSwitchEvent<T> e) {
		try {
			if(!ext2intNodeMap.containsKey(e.getNode()) && !ext2intNodeMap.containsKey(e.getNewParent()))
				throw new NoSuchElementException("Cannot switch parent of node " + e.getNode()+". Either the node or the new parent node has not been reached previously.");

			removeEdge(e.getOldParent(), e.getNode());
			newEdge(e.getNewParent(),e.getNode());

		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	@Subscribe
	public synchronized void receiveNodeRemovedEvent(NodeRemovedEvent<T> e) {
		try {
			graph.removeNode(ext2intNodeMap.get(e.getNode()));
			// ext2intNodeMap.get(e.getNode()).addAttribute("ui.class", e.getType());
			ext2intNodeMap.remove(e.getNode());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private T getNodeOfString(String name) {
		return int2extNodeMap.get(graph.getNode(name));
	}

	public void addNodeListener(NodeListener<T> listener) {
//		this.viewerPipe.addViewerListener(new ViewerListener() {
//
//			@Override
//			public void viewClosed(String arg0) {
//			}
//
//			// @Override
//			// public void mouseOver(String arg0) {
//			// listener.mouseOver(getNodeOfString(arg0));
//			// }
//			//
//			// @Override
//			// public void mouseLeft(String arg0) {
//			// listener.mouseLeft(getNodeOfString(arg0));
//			// }
//
//			@Override
//			public void buttonReleased(String arg0) {
//				listener.buttonReleased(getNodeOfString(arg0));
//			}
//
//			@Override
//			public void buttonPushed(String arg0) {
//				listener.buttonPushed(getNodeOfString(arg0));
//			}
//		});
	}

	public TooltipGenerator<T> getTooltipGenerator() {
		return tooltipGenerator;
	}

	public void setTooltipGenerator(TooltipGenerator<T> tooltipGenerator) {
		this.tooltipGenerator = (TooltipGenerator<T>)tooltipGenerator;
	}

	public void reset(){
		ext2intNodeMap.clear();
		int2extNodeMap.clear();
		graph.clear();
//		graph.addAttribute("ui.stylesheet", "url('conf/searchgraph.css')");


	}
}
