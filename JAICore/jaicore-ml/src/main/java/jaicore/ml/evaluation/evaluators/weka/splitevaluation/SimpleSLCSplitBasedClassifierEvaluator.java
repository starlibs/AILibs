package jaicore.ml.evaluation.evaluators.weka.splitevaluation;

import java.util.ArrayList;
import java.util.List;

import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.IMeasure;
import jaicore.ml.evaluation.IInstancesClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Basic implementation of the {@link AbstractSplitBasedClassifierEvaluator}. Uses the given loss function to compute loss on the given data. No extra steps are performed.
 *
 * @author jnowack
 *
 */
public class SimpleSLCSplitBasedClassifierEvaluator extends AbstractSplitBasedClassifierEvaluator<Double, Double> {

	public SimpleSLCSplitBasedClassifierEvaluator(final IMeasure<Double, Double> basicEvaluator) {
		super(basicEvaluator);
	}

	@Override
	public Double evaluateSplit(final Classifier classifier, final Instances trainingData, final Instances validationData) throws ObjectEvaluationFailedException, InterruptedException {

		try {
			classifier.buildClassifier(trainingData);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new ObjectEvaluationFailedException("Could not build model.", e);
		}

		try {
			List<Double> predicted = new ArrayList<>();
			if (classifier instanceof IInstancesClassifier) {
				for (double prediction : ((IInstancesClassifier) classifier).classifyInstances(validationData)) {
					predicted.add(prediction);
				}
			} else {
				for (Instance inst : validationData) {
					predicted.add(classifier.classifyInstance(inst));
				}
			}
			return this.getBasicEvaluator().calculateAvgMeasure(WekaUtil.getClassesAsList(validationData), predicted);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new ObjectEvaluationFailedException("Could not validate classifier.", e);
		}
	}

}
