package autofe.db.search;

import java.util.ArrayList;
import java.util.List;

import autofe.db.model.database.AbstractAttribute;

public class DatabaseNode {

	private List<AbstractAttribute> selectedAttributes;

	private boolean isFinished;

	public DatabaseNode() {
		selectedAttributes = new ArrayList<>();
		isFinished = false;
	}

	public DatabaseNode(List<AbstractAttribute> selectedAttributes, boolean isFinished) {
		super();
		this.selectedAttributes = selectedAttributes;
		this.isFinished = isFinished;
	}

	public List<AbstractAttribute> getSelectedAttributes() {
		return selectedAttributes;
	}

	public void setSelectedAttributes(List<AbstractAttribute> selectedAttributes) {
		this.selectedAttributes = selectedAttributes;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public String featureString() {
		String toReturn = null;
		if (selectedAttributes.isEmpty()) {
			toReturn = "{}";
		} else {
			StringBuilder sb = new StringBuilder();
			for (AbstractAttribute att : selectedAttributes) {
				sb.append(att.getName());
				sb.append(",");
			}
			toReturn = sb.substring(0, sb.length() - 1);

		}
		return toReturn;
	}

	@Override
	public String toString() {

		return "DatabaseNode [selectedAttributes=" + featureString() + ", isFinished=" + isFinished + "]";
	}

}
