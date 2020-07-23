package ai.libs.jaicore.graphvisualizer.plugin.timeslider;

import ai.libs.jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

public class TimeSliderGUIPluginView extends ASimpleMVCPluginView<TimeSliderGUIPluginModel, TimeSliderGUIPluginController, VBox> {

	private static final String BASE_TEXT = "Timestep";

	private Slider timestepSlider;
	private Label timestepSliderLabel;

	public TimeSliderGUIPluginView(final TimeSliderGUIPluginModel model) {
		super(model, new VBox());
		Platform.runLater(() -> {
			VBox timestepSliderLayout = this.getNode();
			timestepSliderLayout.setAlignment(Pos.CENTER);

			this.timestepSlider = new Slider(0, 1, 0);
			this.timestepSlider.setShowTickLabels(true);
			this.timestepSlider.setShowTickMarks(true);
			this.timestepSlider.setMajorTickUnit(10);
			this.timestepSlider.setMinorTickCount(9);
			this.timestepSlider.setBlockIncrement(1);
			this.timestepSlider.setSnapToTicks(true);

			this.timestepSlider.setOnMouseReleased(event -> this.handleInputEvent());
			this.timestepSlider.setOnKeyPressed(event -> this.handleInputEvent());
			timestepSliderLayout.getChildren().add(this.timestepSlider);

			this.timestepSliderLabel = new Label(BASE_TEXT + ": " + 0 + "/" + model.getMaximumTimeStep());
			timestepSliderLayout.getChildren().add(this.timestepSliderLabel);
			Label spacer = new Label(" ");
			timestepSliderLayout.getChildren().add(spacer);
		});
	}

	public synchronized void handleInputEvent() {
		DefaultGUIEventBus.getInstance().postEvent(new GoToTimeStepEvent((int) this.timestepSlider.getValue()));
	}

	@Override
	public void update() {
		TimeSliderGUIPluginModel model = this.getModel();

		this.timestepSlider.setMax(model.getMaximumTimeStep());
		this.timestepSlider.setValue(model.getCurrentTimeStep());

		int amountOfStepsBetweenMajorTicks = Math.max(10, model.getMaximumTimeStep() / 20);
		this.timestepSlider.setMajorTickUnit(amountOfStepsBetweenMajorTicks);
		this.timestepSlider.setMinorTickCount(amountOfStepsBetweenMajorTicks - 1);

		if (model.isPaused() && this.timestepSlider.isDisabled()) {
			this.timestepSlider.setDisable(false);
		} else if (!model.isPaused() && !this.timestepSlider.isDisabled()) {
			this.timestepSlider.setDisable(true);
		}

		Platform.runLater(() -> this.timestepSliderLabel.setText(BASE_TEXT + ": " + model.getCurrentTimeStep() + "/" + model.getMaximumTimeStep()));
	}

	@Override
	public void clear() {
		/* nothing to do */
	}
}
