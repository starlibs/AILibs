package jaicore.graphvisualizer.window;

import java.util.ArrayList;
import java.util.List;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.graphvisualizer.events.graph.bus.GraphEventSource;
import jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;
import jaicore.graphvisualizer.events.recorder.GraphEventHistoryRecorder;
import jaicore.graphvisualizer.plugin.GUIPlugin;
import jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GraphVisualizationWindow implements Runnable {

	private GraphEventSource graphEventSource;

	private List<GUIPlugin> visualizationPlugins;
	private GraphViewPlugin graphViewPlugin;

	private TabPane pluginTabPane;
	private Slider timestepSlider;
	private Slider visualizationSpeedSlider;

	private BorderPane rootLayout;
	private BorderPane topLayout;
	private ToolBar topButtonToolBar;

	public GraphVisualizationWindow(GraphEventSource graphEventSource, GraphViewPlugin graphViewPlugin, GUIPlugin... visualizationPlugins) {
		this.graphEventSource = graphEventSource;
		initializePlugins(graphEventSource, graphViewPlugin, visualizationPlugins);
	}

	public GraphVisualizationWindow(IAlgorithm<?, ?> algorithm, GraphViewPlugin graphViewPlugin, GUIPlugin... visualizationPlugins) {
		GraphEventHistoryRecorder historyRecorder = new GraphEventHistoryRecorder();
		this.graphEventSource = historyRecorder.getHistory();
		initializePlugins(graphEventSource, graphViewPlugin, visualizationPlugins);
		algorithm.registerListener(historyRecorder);
	}

	private void initializePlugins(GraphEventSource graphEventSource, GraphViewPlugin graphViewPlugin, GUIPlugin... visualizationPlugins) {
		this.graphViewPlugin = graphViewPlugin;
		graphViewPlugin.setGraphEventSource(graphEventSource);
		this.visualizationPlugins = new ArrayList<>(visualizationPlugins.length);
		for (GUIPlugin graphVisualizationPlugin : visualizationPlugins) {
			this.visualizationPlugins.add(graphVisualizationPlugin);
			graphVisualizationPlugin.setGraphEventSource(graphEventSource);
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

	}

	private void initializeTopLayout() {
		topLayout = new BorderPane();
		initializeTopButtonToolBar();
		initializeVisualizationSpeedSlider();
		rootLayout.setTop(topLayout);
	}

	private void initializeTopButtonToolBar() {
		topButtonToolBar = new ToolBar();

		Button startButton = new Button("Start");
		topButtonToolBar.getItems().add(startButton);

		Button pauseButton = new Button("Pause");
		topButtonToolBar.getItems().add(pauseButton);

		Button stepButton = new Button("Step");
		topButtonToolBar.getItems().add(stepButton);

		topButtonToolBar.getItems().add(new Separator());

		Button saveReplayButton = new Button("Save History");
		topButtonToolBar.getItems().add(saveReplayButton);

		Button loadReplayButton = new Button("Load History");
		topButtonToolBar.getItems().add(loadReplayButton);

		topLayout.setTop(topButtonToolBar);
	}

	private void initializeVisualizationSpeedSlider() {
		VBox visualizationSpeedSliderLayout = new VBox();
		visualizationSpeedSliderLayout.setAlignment(Pos.CENTER);

		visualizationSpeedSlider = new Slider(0, 200, 100);
		visualizationSpeedSlider.setShowTickLabels(true);
		visualizationSpeedSlider.setShowTickMarks(true);
		visualizationSpeedSlider.setMajorTickUnit(25);
		visualizationSpeedSlider.setMinorTickCount(5);
		visualizationSpeedSliderLayout.getChildren().add(visualizationSpeedSlider);

		Label visualizationSpeedSliderLabel = new Label("Visualization Speed");
		visualizationSpeedSliderLayout.getChildren().add(visualizationSpeedSliderLabel);

		topLayout.setBottom(visualizationSpeedSliderLayout);
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
		VBox timestepSliderLayout = new VBox();
		timestepSliderLayout.setAlignment(Pos.CENTER);

		timestepSlider = new Slider(0, 1500, 0);
		timestepSlider.setShowTickLabels(true);
		timestepSlider.setShowTickMarks(true);
		timestepSlider.setMajorTickUnit(25);
		timestepSlider.setMinorTickCount(5);
		timestepSliderLayout.getChildren().add(timestepSlider);

		Label timestepSliderLabel = new Label("Timestep");
		timestepSliderLayout.getChildren().add(timestepSliderLabel);

		rootLayout.setBottom(timestepSliderLayout);
	}

	private void initializePlugins() {
		for (GUIPlugin plugin : visualizationPlugins) {
			Tab pluginTab = new Tab(plugin.getView().getTitle(), plugin.getView().getNode());
			pluginTabPane.getTabs().add(pluginTab);
		}
	}

}
