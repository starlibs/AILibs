package ai.libs.jaicore.ml.weka.classification.learner.reduction;

import java.util.HashMap;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import ai.libs.jaicore.basic.sets.Pair;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class ClassifierCache extends HashMap<Integer, Pair<Classifier, Instances>> {

	private static final long serialVersionUID = -8463580964568016772L;

	public Classifier getCachedClassifier(final String classifierName, final EMCNodeType classificationStrategy, final Instances data) {
		int hashCode = new HashCodeBuilder().append(classifierName).append(classificationStrategy).append(data.toString()).toHashCode();
		Pair<Classifier, Instances> pair = this.get(hashCode);
		if (pair == null) {
			return null;
		} else {
			return this.get(hashCode).getX();
		}
	}

	public Instances getCachedTrainingData(final String classifierName, final EMCNodeType classificationStrategy, final Instances data) {
		int hashCode = new HashCodeBuilder().append(classifierName).append(classificationStrategy).append(data.toString()).toHashCode();
		Pair<Classifier, Instances> pair = this.get(hashCode);
		if (pair == null) {
			return null;
		} else {
			return this.get(hashCode).getY();
		}
	}

}
