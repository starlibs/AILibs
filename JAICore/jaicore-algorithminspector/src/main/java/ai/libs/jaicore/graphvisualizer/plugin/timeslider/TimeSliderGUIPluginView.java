package ai.libs.jaicore.graphvisualizer.plugin.timeslider;

import ai.libs.jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

public class TimeSliderGUIPluginView implements IGUIPluginView {

	private TimeSliderGUIPluginModel model;

	private Slider timestepSlider;

	public TimeSliderGUIPluginView() {
		this.model = new TimeSliderGUIPluginModel(this);
	}

	@Override
	public Node getNode() {
		VBox timestepSliderLayout = new VBox();
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
		return timestepSliderLayout;
	}

	public synchronized void handleInputEvent() {
		DefaultGUIEventBus.getInstance().postEvent(new GoToTimeStepEvent((int) this.timestepSlider.getValue()));
	}

	@Override
	public void update() {
		this.timestepSlider.setValue(this.model.getCurrentTimeStep());
		this.timestepSlider.setMax(this.model.getMaximumTimeStep());
		if (this.model.isPaused() && this.timestepSlider.isDisabled()) {
			this.timestepSlider.setDisable(false);
		} else if (!this.model.isPaused() && !this.timestepSlider.isDisabled()) {
			this.timestepSlider.setDisable(true);
		}
	}

	@Override
	public String getTitle() {
		return "Time Slider";
	}

	public TimeSliderGUIPluginModel getModel() {
		return this.model;
	}

}
