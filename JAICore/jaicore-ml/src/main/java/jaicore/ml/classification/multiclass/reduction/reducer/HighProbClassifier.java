package jaicore.ml.classification.multiclass.reduction.reducer;

import java.util.Arrays;

import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class HighProbClassifier implements Classifier {

	private final Classifier c;
	
	
	public HighProbClassifier(Classifier c) {
		super();
		this.c = c;
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		c.buildClassifier(data);
		
	}

	@Override
	public double classifyInstance(Instance instance) throws Exception {
		double selection = -1;
		double best = 0;
		double[] dist = distributionForInstance(instance);
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
	public double[] distributionForInstance(Instance instance) throws Exception {
		return c.distributionForInstance(instance);
	}

	@Override
	public Capabilities getCapabilities() {
		return c.getCapabilities();
	}

}
