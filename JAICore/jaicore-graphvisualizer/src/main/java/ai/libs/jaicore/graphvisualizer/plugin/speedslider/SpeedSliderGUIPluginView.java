package ai.libs.jaicore.graphvisualizer.plugin.speedslider;

import ai.libs.jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginView;
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

		this.visualizationSpeedSlider = new Slider(1, 100, this.model.getCurrentSpeedPercentage());
		this.visualizationSpeedSlider.setShowTickLabels(true);
		this.visualizationSpeedSlider.setShowTickMarks(true);
		this.visualizationSpeedSlider.setMajorTickUnit(5);
		this.visualizationSpeedSlider.setMinorTickCount(1);

		this.visualizationSpeedSlider.setOnMouseReleased(event -> this.handleInputEvent());
		this.visualizationSpeedSlider.setOnKeyPressed(event -> this.handleInputEvent());
		this.visualizationSpeedSlider.setOnKeyReleased(event -> this.handleInputEvent());

		visualizationSpeedSliderLayout.getChildren().add(this.visualizationSpeedSlider);

		Label visualizationSpeedSliderLabel = new Label("Visualization Speed (%)");
		visualizationSpeedSliderLayout.getChildren().add(visualizationSpeedSliderLabel);

		return visualizationSpeedSliderLayout;
	}

	private void handleInputEvent() {
		ChangeSpeedEvent changeSpeedEvent = new ChangeSpeedEvent((int) this.visualizationSpeedSlider.getValue());
		DefaultGUIEventBus.getInstance().postEvent(changeSpeedEvent);
	}

	@Override
	public void update() {
		this.visualizationSpeedSlider.setValue(this.model.getCurrentSpeedPercentage());
	}

	@Override
	public String getTitle() {
		return "Speed Slider";
	}

	public SpeedSliderGUIPluginModel getModel() {
		return this.model;
	}

}
