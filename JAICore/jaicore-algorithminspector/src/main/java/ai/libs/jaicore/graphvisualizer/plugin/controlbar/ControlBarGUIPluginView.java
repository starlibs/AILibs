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
			this.startButton.setMinWidth(70); // this is the size which we need for the "resume" text
			this.startButton.setOnMouseClicked(event -> this.handleStartResumePauseButtonClick());
			topButtonToolBar.getItems().add(this.startButton);

			Button resetButton = new Button("Reset");
			resetButton.setOnMouseClicked(event -> this.handleResetButtonClick());
			topButtonToolBar.getItems().add(resetButton);

			topButtonToolBar.getItems().add(new Separator());
		});
	}

	public void handleStartResumePauseButtonClick() {
		if (this.getModel().isPaused()) {
			DefaultGUIEventBus.getInstance().postEvent(new PlayEvent());
		} else {
			DefaultGUIEventBus.getInstance().postEvent(new PauseEvent());
		}
	}

	public void handleResetButtonClick() {
		DefaultGUIEventBus.getInstance().postEvent(new ResetEvent());
	}

	@Override
	public void update() {
		if (this.getModel().isPaused()) {
			this.startButton.setText("Resume");
		} else {
			this.startButton.setText("Pause ");
		}
	}

	@Override
	public void clear() {
		/* nothing to do */
	}
}
