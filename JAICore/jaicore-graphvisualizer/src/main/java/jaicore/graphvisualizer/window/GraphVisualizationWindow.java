package jaicore.graphvisualizer.window;

import java.util.ArrayList;
import java.util.List;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventSource;
import jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;
import jaicore.graphvisualizer.events.recorder.AlgorithmEventHistory;
import jaicore.graphvisualizer.events.recorder.AlgorithmEventHistoryPuller;
import jaicore.graphvisualizer.events.recorder.AlgorithmEventHistoryRecorder;
import jaicore.graphvisualizer.plugin.IGUIPlugin;
import jaicore.graphvisualizer.plugin.controlbar.ControlBarGUIPlugin;
import jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import jaicore.graphvisualizer.plugin.speedslider.SpeedSliderGUIPlugin;
import jaicore.graphvisualizer.plugin.timeslider.TimeSliderGUIPlugin;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GraphVisualizationWindow implements Runnable {

	private AlgorithmEventSource graphEventSource;
	private AlgorithmEventHistoryPuller algorithmEventHistoryPuller;

	private List<IGUIPlugin> visualizationPlugins;
	private GraphViewPlugin graphViewPlugin;
	private TimeSliderGUIPlugin timeSliderGUIPlugin;
	private ControlBarGUIPlugin controlBarGUIPlugin;
	private SpeedSliderGUIPlugin speedSliderGUIPlugin;

	private TabPane pluginTabPane;

	private BorderPane rootLayout;
	private BorderPane topLayout;

	public GraphVisualizationWindow(AlgorithmEventHistory algorithmEventHistory, GraphViewPlugin graphViewPlugin, IGUIPlugin... visualizationPlugins) {
		algorithmEventHistoryPuller = new AlgorithmEventHistoryPuller(algorithmEventHistory);
		this.graphEventSource = algorithmEventHistoryPuller;
		initializePlugins(graphEventSource, graphViewPlugin, visualizationPlugins);
		// it is important to register the history puller as a last listener!
		DefaultGUIEventBus.getInstance().registerListener(algorithmEventHistoryPuller);
	}

	public GraphVisualizationWindow(IAlgorithm<?, ?> algorithm, GraphViewPlugin graphViewPlugin, IGUIPlugin... visualizationPlugins) {
		AlgorithmEventHistoryRecorder historyRecorder = new AlgorithmEventHistoryRecorder();
		algorithmEventHistoryPuller = new AlgorithmEventHistoryPuller(historyRecorder.getHistory());
		this.graphEventSource = algorithmEventHistoryPuller;
		initializePlugins(graphEventSource, graphViewPlugin, visualizationPlugins);
		algorithm.registerListener(historyRecorder);
		// it is important to register the history puller as a last listener!
		DefaultGUIEventBus.getInstance().registerListener(algorithmEventHistoryPuller);
	}

	private void initializePlugins(AlgorithmEventSource algorithmEventSource, GraphViewPlugin graphViewPlugin, IGUIPlugin... visualizationPlugins) {
		this.graphViewPlugin = graphViewPlugin;
		graphViewPlugin.setAlgorithmEventSource(algorithmEventSource);
		graphViewPlugin.setGUIEventSource(DefaultGUIEventBus.getInstance());

		timeSliderGUIPlugin = new TimeSliderGUIPlugin();
		timeSliderGUIPlugin.setAlgorithmEventSource(algorithmEventSource);
		timeSliderGUIPlugin.setGUIEventSource(DefaultGUIEventBus.getInstance());

		controlBarGUIPlugin = new ControlBarGUIPlugin();
		controlBarGUIPlugin.setAlgorithmEventSource(algorithmEventSource);
		controlBarGUIPlugin.setGUIEventSource(DefaultGUIEventBus.getInstance());

		speedSliderGUIPlugin = new SpeedSliderGUIPlugin();
		speedSliderGUIPlugin.setAlgorithmEventSource(algorithmEventSource);
		speedSliderGUIPlugin.setGUIEventSource(DefaultGUIEventBus.getInstance());

		this.visualizationPlugins = new ArrayList<>(visualizationPlugins.length);
		for (IGUIPlugin graphVisualizationPlugin : visualizationPlugins) {
			this.visualizationPlugins.add(graphVisualizationPlugin);
			graphVisualizationPlugin.setAlgorithmEventSource(algorithmEventSource);
			graphVisualizationPlugin.setGUIEventSource(DefaultGUIEventBus.getInstance());
		}
	}

	@Override
	public void run() {

		rootLayout = new BorderPane();

		initializeTopLayout();

		initializeCenterLayout();

		initializeBottomLayout();

		initializePlugins();

		Scene scene = new Scene(rootLayout, 800, 300);
		Stage stage = new Stage();

		stage.setScene(scene);
		stage.setTitle("MLPlan Graph Search Visualization");
		stage.setMaximized(true);
		stage.show();

		algorithmEventHistoryPuller.start();
	}

	private void initializeTopLayout() {
		topLayout = new BorderPane();
		initializeTopButtonToolBar();
		initializeVisualizationSpeedSlider();
		rootLayout.setTop(topLayout);
	}

	private void initializeTopButtonToolBar() {
		topLayout.setTop(controlBarGUIPlugin.getView().getNode());
	}

	private void initializeVisualizationSpeedSlider() {
		topLayout.setBottom(speedSliderGUIPlugin.getView().getNode());
	}

	private void initializeCenterLayout() {
		SplitPane centerSplitLayout = new SplitPane();
		centerSplitLayout.setDividerPosition(0, 0.25);

		pluginTabPane = new TabPane();
		centerSplitLayout.getItems().add(this.pluginTabPane);

		centerSplitLayout.getItems().add(graphViewPlugin.getView().getNode());

		rootLayout.setCenter(centerSplitLayout);
	}

	private void initializeBottomLayout() {
		rootLayout.setBottom(timeSliderGUIPlugin.getView().getNode());
	}

	private void initializePlugins() {
		for (IGUIPlugin plugin : visualizationPlugins) {
			Tab pluginTab = new Tab(plugin.getView().getTitle(), plugin.getView().getNode());
			pluginTabPane.getTabs().add(pluginTab);
		}
	}

}
