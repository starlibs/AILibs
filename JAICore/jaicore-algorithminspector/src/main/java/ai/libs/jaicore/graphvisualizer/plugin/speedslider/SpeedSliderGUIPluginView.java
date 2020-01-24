package ai.libs.jaicore.graphvisualizer.plugin.speedslider;

import ai.libs.jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

public class SpeedSliderGUIPluginView extends ASimpleMVCPluginView<SpeedSliderGUIPluginModel, SpeedSliderGUIPluginController, VBox> {

	private Slider visualizationSpeedSlider;

	public SpeedSliderGUIPluginView(final SpeedSliderGUIPluginModel model) {
		super (model, new VBox());
		Platform.runLater(() -> {
			VBox visualizationSpeedSliderLayout = this.getNode();
			visualizationSpeedSliderLayout.setAlignment(Pos.CENTER);

			this.visualizationSpeedSlider = new Slider(1, 100, this.getModel().getCurrentSpeedPercentage());
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
		});
	}

	private void handleInputEvent() {
		ChangeSpeedEvent changeSpeedEvent = new ChangeSpeedEvent((int) this.visualizationSpeedSlider.getValue());
		DefaultGUIEventBus.getInstance().postEvent(changeSpeedEvent);
	}

	@Override
	public void update() {
		this.visualizationSpeedSlider.setValue(this.getModel().getCurrentSpeedPercentage());
	}

	@Override
	public String getTitle() {
		return "Speed Slider";
	}

	@Override
	public void clear() {
		/* nothing to do */
	}
}
