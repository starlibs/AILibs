package jaicore.graphvisualizer.plugin.speedslider;

import jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;
import jaicore.graphvisualizer.plugin.IGUIPluginView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

public class SpeedSliderGUIPluginView implements IGUIPluginView {

	private SpeedSliderGUIPluginModel model;

	private Slider visualizationSpeedSlider;

	public SpeedSliderGUIPluginView() {
		this.model = new SpeedSliderGUIPluginModel(this);
	}

	@Override
	public Node getNode() {
		VBox visualizationSpeedSliderLayout = new VBox();
		visualizationSpeedSliderLayout.setAlignment(Pos.CENTER);

		visualizationSpeedSlider = new Slider(1, 100, model.getCurrentSpeedPercentage());
		visualizationSpeedSlider.setShowTickLabels(true);
		visualizationSpeedSlider.setShowTickMarks(true);
		visualizationSpeedSlider.setMajorTickUnit(5);
		visualizationSpeedSlider.setMinorTickCount(1);

		visualizationSpeedSlider.setOnMouseReleased(event -> {
			handleInputEvent();
		});
		visualizationSpeedSlider.setOnKeyPressed(event -> {
			handleInputEvent();
		});
		visualizationSpeedSlider.setOnKeyReleased(event -> {
			handleInputEvent();
		});

		visualizationSpeedSliderLayout.getChildren().add(visualizationSpeedSlider);

		Label visualizationSpeedSliderLabel = new Label("Visualization Speed (%)");
		visualizationSpeedSliderLayout.getChildren().add(visualizationSpeedSliderLabel);

		return visualizationSpeedSliderLayout;
	}

	private void handleInputEvent() {
		ChangeSpeedEvent changeSpeedEvent = new ChangeSpeedEvent((int) visualizationSpeedSlider.getValue());
		DefaultGUIEventBus.getInstance().postEvent(changeSpeedEvent);
	}

	@Override
	public void update() {
		visualizationSpeedSlider.setValue(model.getCurrentSpeedPercentage());
	}

	@Override
	public String getTitle() {
		return "Speed Slider";
	}

	public SpeedSliderGUIPluginModel getModel() {
		return model;
	}

}
