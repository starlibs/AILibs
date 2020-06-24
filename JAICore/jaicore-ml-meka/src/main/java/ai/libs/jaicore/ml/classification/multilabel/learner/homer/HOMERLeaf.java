package ai.libs.jaicore.ml.classification.multilabel.learner.homer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HOMERLeaf extends HOMERNode {

	private Integer label;

	public HOMERLeaf(final Integer label) {
		this.label = label;
	}

	@Override
	public List<HOMERNode> getChildren() {
		return Arrays.asList(this);
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public Collection<Integer> getLabels() {
		return Arrays.asList(this.label);
	}

	@Override
	public String toString() {
		return this.label + "";
	}

}
