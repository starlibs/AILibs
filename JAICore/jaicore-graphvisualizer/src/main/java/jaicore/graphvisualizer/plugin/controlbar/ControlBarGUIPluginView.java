package jaicore.graphvisualizer.plugin.controlbar;

import jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;
import jaicore.graphvisualizer.plugin.IGUIPluginView;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;

public class ControlBarGUIPluginView implements IGUIPluginView {

	private ControlBarGUIPluginModel model;

	private Button startButton;

	public ControlBarGUIPluginView() {
		this.model = new ControlBarGUIPluginModel(this);
	}

	@Override
	public Node getNode() {
		ToolBar topButtonToolBar = new ToolBar();

		startButton = new Button("Play");
		startButton.setOnMouseClicked(event -> handleStartButtonClick());
		topButtonToolBar.getItems().add(startButton);

		Button pauseButton = new Button("Pause");
		pauseButton.setOnMouseClicked(event -> handlePauseButtonClick());
		topButtonToolBar.getItems().add(pauseButton);

		Button resetButton = new Button("Reset");
		resetButton.setOnMouseClicked(event -> handleResetButtonClick());
		topButtonToolBar.getItems().add(resetButton);

		topButtonToolBar.getItems().add(new Separator());

		// Button saveReplayButton = new Button("Save History");
		// topButtonToolBar.getItems().add(saveReplayButton);
		//
		// Button loadReplayButton = new Button("Load History");
		// topButtonToolBar.getItems().add(loadReplayButton);
		return topButtonToolBar;
	}

	public void handleStartButtonClick() {
		DefaultGUIEventBus.getInstance().postEvent(new PlayEvent());
	}

	public void handlePauseButtonClick() {
		DefaultGUIEventBus.getInstance().postEvent(new PauseEvent());
	}

	public void handleResetButtonClick() {
		DefaultGUIEventBus.getInstance().postEvent(new ResetEvent());
	}

	@Override
	public void update() {
		if (model.isPaused()) {
			startButton.setText("Resume");
			startButton.setDisable(false);
		} else {
			startButton.setText("Play");
			startButton.setDisable(true);
		}
	}

	@Override
	public String getTitle() {
		return "Control Bar";
	}

	public ControlBarGUIPluginModel getModel() {
		return model;
	}

}
