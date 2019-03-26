package jaicore.graphvisualizer.plugin.timeslider;

import jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;
import jaicore.graphvisualizer.plugin.IGUIPluginView;
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

		timestepSlider = new Slider(0, 1, 0);
		timestepSlider.setShowTickLabels(true);
		timestepSlider.setShowTickMarks(true);
		timestepSlider.setMajorTickUnit(25);
		timestepSlider.setMinorTickCount(5);

		timestepSlider.setOnMouseReleased(event -> {
			handleInputEvent();
		});
		timestepSlider.setOnKeyPressed(event -> {
			handleInputEvent();
		});
		timestepSlider.setOnKeyReleased(event -> {
			handleInputEvent();
		});

		timestepSliderLayout.getChildren().add(timestepSlider);

		Label timestepSliderLabel = new Label("Timestep");
		timestepSliderLayout.getChildren().add(timestepSliderLabel);
		return timestepSliderLayout;
	}

	public synchronized void handleInputEvent() {
		DefaultGUIEventBus.getInstance().postEvent(new GoToTimeStepEvent((int) timestepSlider.getValue()));
	}

	@Override
	public void update() {
		timestepSlider.setValue(model.getCurrentTimeStep());
		timestepSlider.setMax(model.getMaximumTimeStep());

		if (model.isPaused() && timestepSlider.isDisabled()) {
			timestepSlider.setDisable(false);
		} else if (!model.isPaused() && !timestepSlider.isDisabled()) {
			timestepSlider.setDisable(true);
		}
	}

	@Override
	public String getTitle() {
		return "Time Slider";
	}

	public TimeSliderGUIPluginModel getModel() {
		return model;
	}

}
