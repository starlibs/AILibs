package ai.libs.jaicore.ml.classification.multilabel.learner.homer;

import java.util.Arrays;
import java.util.Collection;

public class HOMERLeaf extends HOMERNode {

	private Integer label;

	public HOMERLeaf(final Integer label) {
		this.label = label;
	}

	public Collection<Integer> getLabel() {
		return Arrays.asList(this.label);
	}

	@Override
	public String toString() {
		return this.label + "";
	}

}
