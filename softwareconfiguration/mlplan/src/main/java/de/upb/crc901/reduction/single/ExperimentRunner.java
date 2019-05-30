package de.upb.crc901.reduction.single;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.ml.WekaUtil;
import jaicore.ml.classification.multiclass.reduction.MCTreeNodeReD;
import jaicore.ml.classification.multiclass.reduction.splitters.ISplitter;
import jaicore.ml.classification.multiclass.reduction.splitters.ISplitterFactory;
import jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import jaicore.ml.evaluation.evaluators.weka.FixedSplitClassifierEvaluator;
import jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.SimpleSLCSplitBasedClassifierEvaluator;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class ExperimentRunner<T extends ISplitter> {

	private final int k;
	private final int mccvRepeats;
	private final ISplitterFactory<T> splitterFactory;

	public ExperimentRunner(final int k, final int mccvRepeats, final ISplitterFactory<T> splitterFactory) {
		super();
		this.k = k;
		this.mccvRepeats = mccvRepeats;
		this.splitterFactory = splitterFactory;
	}


	public Map<String, Object> conductSingleOneStepReductionExperiment(final ReductionExperiment experiment) throws Exception {

		/* load data */
		Instances data = new Instances(new BufferedReader(new FileReader(experiment.getDataset())));
		data.setClassIndex(data.numAttributes() - 1);

		/* prepare basis for experiments */
		int seed = experiment.getSeed();
		Classifier leftClassifier = AbstractClassifier.forName(experiment.getNameOfLeftClassifier(), null);
		Classifier innerClassifier = AbstractClassifier.forName(experiment.getNameOfInnerClassifier(), null);
		Classifier rightClassifier = AbstractClassifier.forName(experiment.getNameOfRightClassifier(), null);
		List<Instances> outerSplit = WekaUtil.getStratifiedSplit(data, experiment.getSeed(), .7);
		MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(new SimpleSLCSplitBasedClassifierEvaluator(new ZeroOneLoss()), this.mccvRepeats, outerSplit.get(0), .7, seed);
		ISplitter splitter = this.splitterFactory.getSplitter(seed);

		/* compute best of k splits */
		MCTreeNodeReD bestFoundClassifier = null;
		double bestFoundScore = Double.MAX_VALUE;
		for (int i = 0; i < this.k; i++) {
			List<Collection<String>> classSplit;
			try {
				classSplit = new ArrayList<>(splitter.split(outerSplit.get(0)));
			} catch (Throwable e) {
				throw new RuntimeException("Could not create a split.", e);
			}
			MCTreeNodeReD classifier = new MCTreeNodeReD(innerClassifier, classSplit.get(0), leftClassifier, classSplit.get(1), rightClassifier);
			double loss = mccv.evaluate(classifier);
			System.out.println("\t\t\tComputed loss " + loss);
			if (loss < bestFoundScore) {
				bestFoundScore = loss;
				bestFoundClassifier = classifier;
			}
		}

		/* train classifier on all data */
		double loss = new FixedSplitClassifierEvaluator(outerSplit.get(0), outerSplit.get(1)).evaluate(bestFoundClassifier);
		Map<String, Object> result = new HashMap<>();
		System.out.println("\t\t\tBest previously observed loss was " + bestFoundScore + ". The retrained classifier achieves " + loss + " on the full data.");
		result.put("errorRate", loss);
		//		result.put("trainTime", time);
		return result;
	}
}
