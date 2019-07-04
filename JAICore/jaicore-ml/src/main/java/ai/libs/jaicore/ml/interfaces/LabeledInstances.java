package ai.libs.jaicore.ml.interfaces;

import java.util.List;

public interface LabeledInstances<L> extends Instances<LabeledInstance<L>> {
	public List<L> getOccurringLabels();
}
