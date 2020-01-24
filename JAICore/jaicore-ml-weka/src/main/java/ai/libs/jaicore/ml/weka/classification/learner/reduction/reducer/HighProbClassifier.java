package ai.libs.jaicore.ml.weka.classification.learner.reduction.reducer;

import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class HighProbClassifier implements Classifier {
	private static final long serialVersionUID = -139880264457589983L;
	private final Classifier c;

	public HighProbClassifier(final Classifier c) {
		super();
		this.c = c;
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		this.c.buildClassifier(data);

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

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		return this.c.distributionForInstance(instance);
	}

	@Override
	public Capabilities getCapabilities() {
		return this.c.getCapabilities();
	}

}
