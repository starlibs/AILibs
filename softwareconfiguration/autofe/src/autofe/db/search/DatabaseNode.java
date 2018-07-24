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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (selectedAttributes.isEmpty()) {
			sb.append("{}");
		} else {
			for (AbstractAttribute att : selectedAttributes) {
				sb.append(att.getName());
				sb.append(",");
			}
		}
		return "DatabaseNode [selectedAttributes=" + sb.substring(0, sb.length() - 1) + ", isFinished=" + isFinished
				+ "]";
	}

}
