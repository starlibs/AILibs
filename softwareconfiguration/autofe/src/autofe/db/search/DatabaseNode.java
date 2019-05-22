package autofe.db.search;

import java.util.ArrayList;
import java.util.List;

import autofe.db.model.database.AbstractFeature;
import autofe.db.model.database.Attribute;

public class DatabaseNode {

	private List<AbstractFeature> selectedFeatures;

	private boolean isFinished;

	public DatabaseNode() {
		selectedFeatures = new ArrayList<>();
		isFinished = false;
	}

	public DatabaseNode(List<AbstractFeature> selectedFeatures, boolean isFinished) {
		super();
		this.selectedFeatures = selectedFeatures;
		this.isFinished = isFinished;
	}

	public List<AbstractFeature> getSelectedFeatures() {
		return selectedFeatures;
	}

	public void setSelectedFeatures(List<AbstractFeature> selectedFeatures) {
		this.selectedFeatures = selectedFeatures;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public String featureString() {
		String toReturn = null;
		if (selectedFeatures.isEmpty()) {
			toReturn = "{}";
		} else {
			StringBuilder sb = new StringBuilder();
			for (AbstractFeature feature : selectedFeatures) {
				sb.append(feature.getName());
				sb.append(",");
			}
			toReturn = sb.substring(0, sb.length() - 1);

		}
		return toReturn;
	}

	public boolean containsAttribute(Attribute attribute) {
		for (AbstractFeature abstractFeature : selectedFeatures) {
			if (abstractFeature.getParent().equals(attribute)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {

		return "DatabaseNode [selectedFeatures=" + featureString() + ", isFinished=" + isFinished + "]";
	}

}
