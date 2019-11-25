package ai.libs.jaicore.ml.weka.classification.learner.reduction;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Instance;

public abstract class AMCTreeNode<C> implements Classifier, Serializable {

	protected final List<C> containedClasses;

	public AMCTreeNode(final List<C> containedClasses) {
		super();
		this.containedClasses = containedClasses;
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		double selection = -1;
		double best = 0;
		double[] dist = this.distributionForInstance(instance);
		for (int i = 0; i < dist.length; i++) {
			double score = dist[i];
			if (score > best) {
				best = score;
				selection = i;
			}
		}
		return selection;
	}

	public Collection<C> getContainedClasses() {
		return this.containedClasses;
	}
}
