package ai.libs.jaicore.graphvisualizer.plugin.timeslider;

import ai.libs.jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

public class TimeSliderGUIPluginView extends ASimpleMVCPluginView<TimeSliderGUIPluginModel, TimeSliderGUIPluginController, VBox> {

	private Slider timestepSlider;

	public TimeSliderGUIPluginView(final TimeSliderGUIPluginModel model) {
		super (model, new VBox());
		Platform.runLater(() -> {
			VBox timestepSliderLayout = this.getNode();
			timestepSliderLayout.setAlignment(Pos.CENTER);

			this.timestepSlider = new Slider(0, 1, 0);
			this.timestepSlider.setShowTickLabels(false);
			this.timestepSlider.setShowTickMarks(false);

			this.timestepSlider.setOnMouseReleased(event -> this.handleInputEvent());
			this.timestepSlider.setOnKeyPressed(event -> this.handleInputEvent());
			this.timestepSlider.setOnKeyReleased(event -> this.handleInputEvent());
			timestepSliderLayout.getChildren().add(this.timestepSlider);

			Label timestepSliderLabel = new Label("Timestep");
			timestepSliderLayout.getChildren().add(timestepSliderLabel);
		});
	}

	public synchronized void handleInputEvent() {
		DefaultGUIEventBus.getInstance().postEvent(new GoToTimeStepEvent((int) this.timestepSlider.getValue()));
	}

	@Override
	public void update() {
		TimeSliderGUIPluginModel model = this.getModel();
		this.timestepSlider.setValue(model.getCurrentTimeStep());
		this.timestepSlider.setMax(model.getMaximumTimeStep());
		if (model.isPaused() && this.timestepSlider.isDisabled()) {
			this.timestepSlider.setDisable(false);
		} else if (!model.isPaused() && !this.timestepSlider.isDisabled()) {
			this.timestepSlider.setDisable(true);
		}
	}

	@Override
	public String getTitle() {
		return "Time Slider";
	}

	@Override
	public void clear() {
		/* nothing to do */
	}
}
