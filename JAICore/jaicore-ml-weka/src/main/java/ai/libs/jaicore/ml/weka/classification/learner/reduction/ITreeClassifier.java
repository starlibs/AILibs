package ai.libs.jaicore.ml.weka.classification.learner.reduction;

import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Instance;

public interface ITreeClassifier extends Classifier {

	public int getHeight();

	@Override
	public double classifyInstance(final Instance instance) throws Exception;

	public int getDepthOfFirstCommonParent(List<Integer> classes);
}
