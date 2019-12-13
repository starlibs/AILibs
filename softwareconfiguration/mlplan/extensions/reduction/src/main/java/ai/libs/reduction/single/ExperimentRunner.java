package ai.libs.reduction.single;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.api4.java.ai.ml.classification.IClassifierEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.evaluation.evaluator.FixedSplitClassifierEvaluator;
import ai.libs.jaicore.ml.core.evaluation.evaluator.MonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.jaicore.ml.weka.classification.learner.reduction.MCTreeNodeReD;
import ai.libs.jaicore.ml.weka.classification.learner.reduction.splitter.ISplitter;
import ai.libs.jaicore.ml.weka.classification.learner.reduction.splitter.ISplitterFactory;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class ExperimentRunner<T extends ISplitter> {

	private final int k;
	private final int mccvRepeats;
	private final ISplitterFactory<T> splitterFactory;
	private final Logger logger = LoggerFactory.getLogger(ExperimentRunner.class);

	public ExperimentRunner(final int k, final int mccvRepeats, final ISplitterFactory<T> splitterFactory) {
		super();
		this.k = k;
		this.mccvRepeats = mccvRepeats;
		this.splitterFactory = splitterFactory;
	}

	public Map<String, Object> conductSingleOneStepReductionExperiment(final ReductionExperiment experiment) throws Exception {

		/* load data */
		Instances instances = new Instances(new BufferedReader(new FileReader(experiment.getDataset())));
		instances.setClassIndex(instances.numAttributes() - 1);
		IWekaInstances data = new WekaInstances(instances);

		/* prepare basis for experiments */
		int seed = experiment.getSeed();
		Classifier leftClassifier = AbstractClassifier.forName(experiment.getNameOfLeftClassifier(), null);
		Classifier innerClassifier = AbstractClassifier.forName(experiment.getNameOfInnerClassifier(), null);
		Classifier rightClassifier = AbstractClassifier.forName(experiment.getNameOfRightClassifier(), null);
		List<IWekaInstances> outerSplit = WekaUtil.getStratifiedSplit(data, experiment.getSeed(), .7);
		IClassifierEvaluator mccv = new MonteCarloCrossValidationEvaluator(new WekaInstances(data), this.mccvRepeats, .7, new Random(seed));
		ISplitter splitter = this.splitterFactory.getSplitter(seed);

		/* compute best of k splits */
		MCTreeNodeReD bestFoundClassifier = null;
		double bestFoundScore = Double.MAX_VALUE;
		for (int i = 0; i < this.k; i++) {
			List<Collection<String>> classSplit;
			try {
				classSplit = new ArrayList<>(splitter.split(outerSplit.get(0).getInstances()));
			} catch (Exception e) {
				throw new ExperimentEvaluationFailedException("Could not create a split.", e);
			}
			MCTreeNodeReD classifier = new MCTreeNodeReD(innerClassifier, classSplit.get(0), leftClassifier, classSplit.get(1), rightClassifier);
			double loss = mccv.evaluate(new WekaClassifier(classifier));
			this.logger.info("\t\t\tComputed loss {}", loss);
			if (loss < bestFoundScore) {
				bestFoundScore = loss;
				bestFoundClassifier = classifier;
			}
		}

		/* train classifier on all data */
		double loss = new FixedSplitClassifierEvaluator(outerSplit.get(0), outerSplit.get(1), EClassificationPerformanceMeasure.ERRORRATE).evaluate(new WekaClassifier(bestFoundClassifier));
		Map<String, Object> result = new HashMap<>();
		this.logger.info("\t\t\tBest previously observed loss was {}. The retrained classifier achieves {} on the full data.", bestFoundScore, loss);
		result.put("errorRate", loss);
		return result;
	}
}
