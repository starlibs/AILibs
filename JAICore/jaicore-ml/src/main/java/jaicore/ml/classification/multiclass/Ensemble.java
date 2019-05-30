package jaicore.ml.classification.multiclass;

import java.util.LinkedList;

import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class Ensemble extends LinkedList<Classifier> implements Classifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4094417200499766748L;

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		for (Classifier c : this) {
			c.buildClassifier(data);
		}
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		int best = 0;
		double[] dist = this.distributionForInstance(instance);
		for (int i = 1; i < dist.length; i++) {
			if (dist[i] > dist[best]) {
				best = i;
			}
		}
		return best;
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		double[] sums = new double[instance.classAttribute().numValues()], newProbs;
		for (Classifier c : this) {
			newProbs = c.distributionForInstance(instance);
			for (int j = 0; j < newProbs.length; j++) {
				sums[j] += newProbs[j];
			}
		}
		if (Utils.eq(Utils.sum(sums), 1)) {
			return sums;
		} else {
			Utils.normalize(sums);
			return sums;
		}
	}

	@Override
	public Capabilities getCapabilities() {
		return null;
	}

}
