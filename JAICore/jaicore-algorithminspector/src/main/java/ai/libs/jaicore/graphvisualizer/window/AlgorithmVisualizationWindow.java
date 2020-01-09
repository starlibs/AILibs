package ai.libs.jaicore.graphvisualizer.window;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.events.IAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;
import ai.libs.jaicore.graphvisualizer.events.recorder.AlgorithmEventHistory;
import ai.libs.jaicore.graphvisualizer.events.recorder.AlgorithmEventHistoryEntryDeliverer;
import ai.libs.jaicore.graphvisualizer.events.recorder.AlgorithmEventHistoryRecorder;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyProcessedAlgorithmEventSource;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.ControlBarGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.speedslider.SpeedSliderGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.timeslider.TimeSliderGUIPlugin;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * An {@link AlgorithmVisualizationWindow} can be created to have a visualization of the behavior of an algorithm. We generally differentiate between a live version of an {@link IAlgorithm} run for which an instance of that algorithm and a
 * list of {@link AlgorithmEventPropertyComputer}s is required and an offline run, for which only an {@link AlgorithmEventHistory} is required. In the first case, the property computers are used to extract relevant information which is
 * required by the {@link IGUIPlugin}s (to be displayed) from the underlying {@link IAlgorithmEvent}s which are provided by the actual algorithm. When playing a recording, these computers are not required as the data was already computed as
 * is stored as part of the replay in the {@link AlgorithmEventHistory}. Furthermore it requires a main {@link IGUIPlugin} which will
 * be displayed as the main element and optionally an unbounded amount of additional {@link IGUIPlugin} which will be displayed in order to provide additional information.
 *
 * @author atornede
 *
 */
public class AlgorithmVisualizationWindow implements Runnable {

	private PropertyProcessedAlgorithmEventSource algorithmEventSource;
	private AlgorithmEventHistoryEntryDeliverer algorithmEventHistoryPuller;

	private AlgorithmEventHistory algorithmEventHistory;

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

	/**
	 * Creates a new {@link AlgorithmVisualizationWindow} based on the given {@link AlgorithmEventHistory} (i.e. offline version), the main {@link IGUIPlugin} and optionally additional plugins.
	 *
	 * @param algorithmEventHistory The {@link AlgorithmEventHistory} providing data to be displayed.
	 * @param mainPlugin The main {@link IGUIPlugin} which will be displayed as the main information source.
	 * @param visualizationPlugins A list of additional {@link IGUIPlugin}s displaying side information.
	 */
	public AlgorithmVisualizationWindow(final AlgorithmEventHistory algorithmEventHistory, final IGUIPlugin mainPlugin, final IGUIPlugin... visualizationPlugins) {
		this.mainPlugin = mainPlugin;
		this.algorithmEventHistory = algorithmEventHistory;
		this.algorithmEventHistoryPuller = new AlgorithmEventHistoryEntryDeliverer(algorithmEventHistory);
		this.algorithmEventSource = this.algorithmEventHistoryPuller;
		this.initializePlugins(visualizationPlugins);
		// it is important to register the history puller as a last listener!
		DefaultGUIEventBus.getInstance().registerListener(this.algorithmEventHistoryPuller);
	}

	/**
	 * Creates a new {@link AlgorithmVisualizationWindow} based on the given {@link IAlgorithm}, the list of {@link AlgorithmEventPropertyComputer}s, a main {@link IGUIPlugin} and optionally additional plugins. This constructor should be
	 * used when using a visualization in an online run.
	 *
	 * @param algorithm The {@link IAlgorithm} yielding information to be displayed.
	 * @param algorithmEventPropertyComputers The {@link AlgorithmEventPropertyComputer}s computing all information from the {@link IAlgorithmEvent}s provided by the {@link IAlgorithm} which are required by the {@link IGUIPlugin}s which are
	 *            registered.
	 * @param mainPlugin The main {@link IGUIPlugin} which will be displayed as the main information source.
	 * @param visualizationPlugins A list of additional {@link IGUIPlugin}s displaying side information.
	 */
	public AlgorithmVisualizationWindow(final IAlgorithm<?, ?> algorithm, final List<AlgorithmEventPropertyComputer> algorithmEventPropertyComputers, final IGUIPlugin mainPlugin, final IGUIPlugin... visualizationPlugins) {
		this.mainPlugin = mainPlugin;
		AlgorithmEventHistoryRecorder historyRecorder = new AlgorithmEventHistoryRecorder(algorithmEventPropertyComputers);
		algorithm.registerListener(historyRecorder);
		this.algorithmEventHistory = historyRecorder.getHistory();
		this.algorithmEventHistoryPuller = new AlgorithmEventHistoryEntryDeliverer(historyRecorder.getHistory());
		this.algorithmEventSource = this.algorithmEventHistoryPuller;
		this.initializePlugins(visualizationPlugins);
		// it is important to register the history puller as a last listener!
		DefaultGUIEventBus.getInstance().registerListener(this.algorithmEventHistoryPuller);
	}

	private void initializePlugins(final IGUIPlugin... visualizationPlugins) {
		this.mainPlugin.setAlgorithmEventSource(this.algorithmEventSource);
		this.mainPlugin.setGUIEventSource(DefaultGUIEventBus.getInstance());

		this.timeSliderGUIPlugin = new TimeSliderGUIPlugin();
		this.timeSliderGUIPlugin.setAlgorithmEventSource(this.algorithmEventSource);
		this.timeSliderGUIPlugin.setGUIEventSource(DefaultGUIEventBus.getInstance());

		this.controlBarGUIPlugin = new ControlBarGUIPlugin();
		this.controlBarGUIPlugin.setAlgorithmEventSource(this.algorithmEventSource);
		this.controlBarGUIPlugin.setGUIEventSource(DefaultGUIEventBus.getInstance());

		this.speedSliderGUIPlugin = new SpeedSliderGUIPlugin();
		this.speedSliderGUIPlugin.setAlgorithmEventSource(this.algorithmEventSource);
		this.speedSliderGUIPlugin.setGUIEventSource(DefaultGUIEventBus.getInstance());

		this.visualizationPlugins = new ArrayList<>(visualizationPlugins.length);
		for (IGUIPlugin graphVisualizationPlugin : visualizationPlugins) {
			this.visualizationPlugins.add(graphVisualizationPlugin);
			graphVisualizationPlugin.setAlgorithmEventSource(this.algorithmEventSource);
			graphVisualizationPlugin.setGUIEventSource(DefaultGUIEventBus.getInstance());
		}
	}

	public void show() {
		Platform.runLater(this);
	}

	@Override
	public void run() {

		this.rootLayout = new BorderPane();

		this.initializeTopLayout();

		this.initializeCenterLayout();

		this.initializeBottomLayout();

		this.initializePluginTabs();

		Scene scene = new Scene(this.rootLayout, 800, 300);
		this.stage = new Stage();

		this.stage.setScene(scene);
		this.stage.setTitle(this.title);
		this.stage.setMaximized(true);
		this.stage.show();

		this.algorithmEventHistoryPuller.start();
	}

	private void initializeTopLayout() {
		this.topLayout = new BorderPane();
		this.initializeTopButtonToolBar();
		this.initializeVisualizationSpeedSlider();
		this.rootLayout.setTop(this.topLayout);
	}

	private void initializeTopButtonToolBar() {
		this.topLayout.setTop(this.controlBarGUIPlugin.getView().getNode());
	}

	private void initializeVisualizationSpeedSlider() {
		this.topLayout.setBottom(this.speedSliderGUIPlugin.getView().getNode());
	}

	private void initializeCenterLayout() {
		SplitPane centerSplitLayout = new SplitPane();
		centerSplitLayout.setDividerPosition(0, 0.25);

		this.pluginTabPane = new TabPane();
		centerSplitLayout.getItems().add(this.pluginTabPane);
		centerSplitLayout.getItems().add(this.mainPlugin.getView().getNode());

		this.rootLayout.setCenter(centerSplitLayout);
	}

	private void initializeBottomLayout() {
		this.rootLayout.setBottom(this.timeSliderGUIPlugin.getView().getNode());
	}

	private void initializePluginTabs() {
		for (IGUIPlugin plugin : this.visualizationPlugins) {
			Tab pluginTab = new Tab(plugin.getView().getTitle(), plugin.getView().getNode());
			this.pluginTabPane.getTabs().add(pluginTab);
		}
	}

	/**
	 * Sets the title of this window to the given {@link String}.
	 *
	 * @param title The new title of this window.
	 */
	public void setTitle(final String title) {
		this.title = title;
		this.stage.setTitle(title);
	}

	/**
	 * Returns the underlying {@link AlgorithmEventHistory}.
	 *
	 * @return The underlying {@link AlgorithmEventHistory}.
	 */
	public AlgorithmEventHistory getAlgorithmEventHistory() {
		return this.algorithmEventHistory;
	}
}
