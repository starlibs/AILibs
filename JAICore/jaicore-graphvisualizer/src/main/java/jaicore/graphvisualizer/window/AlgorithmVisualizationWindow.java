package jaicore.graphvisualizer.window;

import java.util.ArrayList;
import java.util.List;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventSource;
import jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;
import jaicore.graphvisualizer.events.recorder.AlgorithmEventHistory;
import jaicore.graphvisualizer.events.recorder.AlgorithmEventHistoryEntryDeliverer;
import jaicore.graphvisualizer.events.recorder.AlgorithmEventHistoryRecorder;
import jaicore.graphvisualizer.plugin.IGUIPlugin;
import jaicore.graphvisualizer.plugin.controlbar.ControlBarGUIPlugin;
import jaicore.graphvisualizer.plugin.speedslider.SpeedSliderGUIPlugin;
import jaicore.graphvisualizer.plugin.timeslider.TimeSliderGUIPlugin;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AlgorithmVisualizationWindow implements Runnable {

	private AlgorithmEventSource algorithmEventSource;
	private AlgorithmEventHistoryEntryDeliverer algorithmEventHistoryPuller;

	private List<IGUIPlugin> visualizationPlugins;

	private IGUIPlugin mainPlugin;
	private TimeSliderGUIPlugin timeSliderGUIPlugin;
	private ControlBarGUIPlugin controlBarGUIPlugin;
	private SpeedSliderGUIPlugin speedSliderGUIPlugin;

	private String title = "MLPlan Graph Search Visualization";

	private Stage stage;

	private BorderPane rootLayout;
	private BorderPane topLayout;

	private TabPane pluginTabPane;

	public AlgorithmVisualizationWindow(AlgorithmEventHistory algorithmEventHistory, IGUIPlugin mainPlugin, IGUIPlugin... visualizationPlugins) {
		this.mainPlugin = mainPlugin;
		algorithmEventHistoryPuller = new AlgorithmEventHistoryEntryDeliverer(algorithmEventHistory);
		this.algorithmEventSource = algorithmEventHistoryPuller;
		initializePlugins(visualizationPlugins);
		// it is important to register the history puller as a last listener!
		DefaultGUIEventBus.getInstance().registerListener(algorithmEventHistoryPuller);
	}

	public AlgorithmVisualizationWindow(IAlgorithm<?, ?> algorithm, IGUIPlugin mainPlugin, IGUIPlugin... visualizationPlugins) {
		this.mainPlugin = mainPlugin;
		AlgorithmEventHistoryRecorder historyRecorder = new AlgorithmEventHistoryRecorder();
		algorithm.registerListener(historyRecorder);
		algorithmEventHistoryPuller = new AlgorithmEventHistoryEntryDeliverer(historyRecorder.getHistory());
		this.algorithmEventSource = algorithmEventHistoryPuller;
		initializePlugins(visualizationPlugins);
		// it is important to register the history puller as a last listener!
		DefaultGUIEventBus.getInstance().registerListener(algorithmEventHistoryPuller);
	}

	private void initializePlugins(IGUIPlugin... visualizationPlugins) {
		mainPlugin.setAlgorithmEventSource(algorithmEventSource);
		mainPlugin.setGUIEventSource(DefaultGUIEventBus.getInstance());

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

		initializePluginTabs();

		Scene scene = new Scene(rootLayout, 800, 300);
		stage = new Stage();

		stage.setScene(scene);
		stage.setTitle(title);
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
		centerSplitLayout.getItems().add(mainPlugin.getView().getNode());

		rootLayout.setCenter(centerSplitLayout);
	}

	private void initializeBottomLayout() {
		rootLayout.setBottom(timeSliderGUIPlugin.getView().getNode());
	}

	private void initializePluginTabs() {
		for (IGUIPlugin plugin : visualizationPlugins) {
			Tab pluginTab = new Tab(plugin.getView().getTitle(), plugin.getView().getNode());
			pluginTabPane.getTabs().add(pluginTab);
		}
	}

	public void setTitle(String title) {
		this.title = title;
		stage.setTitle(title);
	}

}
