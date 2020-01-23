package ai.libs.jaicore.graphvisualizer.plugin.controlbar;

import ai.libs.jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;

public class ControlBarGUIPluginView extends ASimpleMVCPluginView<ControlBarGUIPluginModel, ControlBarGUIPluginController, ToolBar> {

	private Button startButton;

	public ControlBarGUIPluginView(final ControlBarGUIPluginModel model) {
		super(model, new ToolBar());

		Platform.runLater(() -> {

			ToolBar topButtonToolBar = this.getNode();
			this.startButton = new Button("Play");
			this.startButton.setOnMouseClicked(event -> this.handleStartButtonClick());
			topButtonToolBar.getItems().add(this.startButton);

			Button pauseButton = new Button("Pause");
			pauseButton.setOnMouseClicked(event -> this.handlePauseButtonClick());
			topButtonToolBar.getItems().add(pauseButton);

			Button resetButton = new Button("Reset");
			resetButton.setOnMouseClicked(event -> this.handleResetButtonClick());
			topButtonToolBar.getItems().add(resetButton);

			topButtonToolBar.getItems().add(new Separator());
		});
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
		if (this.getModel().isPaused()) {
			this.startButton.setText("Resume");
			this.startButton.setDisable(false);
		} else {
			this.startButton.setText("Play");
			this.startButton.setDisable(true);
		}
	}

	@Override
	public String getTitle() {
		return "Control Bar";
	}

	@Override
	public void clear() {
		/* nothing to do */
	}
}
