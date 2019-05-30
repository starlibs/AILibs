package hasco.gui.statsplugin;

import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;

/**
 * 
 * @author fmohr
 * 
 *         This class provides the cells for the trees in this view. They are composed of a label with the name of the required interface and the combo box with the available components.
 *
 */
public class HASCOModelStatisticsComponentCell extends TreeCell<HASCOModelStatisticsComponentSelector> {

	private final TreeView<HASCOModelStatisticsComponentSelector> tv;

	public HASCOModelStatisticsComponentCell(TreeView<HASCOModelStatisticsComponentSelector> tv) {
		super();
		this.tv = tv;
	}

	@Override
	public void updateItem(HASCOModelStatisticsComponentSelector item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			return;
		}
		if (item != null) {
			String requiredInterface = item.getRequiredInterface();
			HBox entry = new HBox();
			entry.getChildren().add(new Label((requiredInterface != null ? requiredInterface : "Root") + ": "));
			entry.getChildren().add(item.getComponentSelector());
			setGraphic(entry);
		}
	}

	public TreeView<HASCOModelStatisticsComponentSelector> getTv() {
		return tv;
	}
}
