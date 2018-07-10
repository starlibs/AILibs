package autofe.algorithm.hasco.evaluation;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.EvaluationUtils;
import jaicore.ml.WekaUtil;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

public class EnsembleObjectEvaluator extends AbstractHASCOFEObjectEvaluator {
	private static final Logger logger = LoggerFactory.getLogger(EnsembleObjectEvaluator.class);

	@Override
	public Double evaluate(FilterPipeline pipeline) throws Exception {
		logger.debug("Applying and evaluating pipeline " + pipeline.toString());
		DataSet dataSet = pipeline.applyFilter(this.data, true);

		logger.debug("Applied pipeline. Starting benchmarking...");

		// TODO
		ReliefFAttributeEval relief = new ReliefFAttributeEval();
		relief.buildEvaluator(dataSet.getInstances());

		double attEvalSum = 0;
		for (int i = 0; i < dataSet.getInstances().numAttributes(); i++) {
			attEvalSum += relief.evaluateAttribute(i);
		}
		attEvalSum /= dataSet.getInstances().numAttributes();

		/* Variance */
		double varianceMean = 0;
		int totalNumericCount = 0;
		for (int i = 0; i < dataSet.getInstances().numAttributes() - 1; i++) {
			if (dataSet.getInstances().attribute(i).isNumeric()) {
				dataSet.getInstances().attributeStats(i).numericStats.calculateDerived();
				varianceMean += Math.pow(dataSet.getInstances().attributeStats(i).numericStats.stdDev, 2);
				totalNumericCount++;
			}
		}
		varianceMean /= totalNumericCount;

		/* KNN */
		List<Instances> split = WekaUtil.getStratifiedSplit(this.data.getInstances(), new Random(42), .7f);
		IBk knn = new IBk(10);
		knn.buildClassifier(split.get(0));
		Evaluation eval = new Evaluation(split.get(0));
		eval.evaluateModel(knn, split.get(1));
		double knnResult = eval.pctCorrect() / 100d;

		double finalScore = 1 - (0.33 * attEvalSum + 0.33 * knnResult + 0.33 * varianceMean)
				+ ATT_COUNT_PENALTY * EvaluationUtils.calculateAttributeCountPenalty(this.data.getInstances());

		logger.info("Ensemble result: " + finalScore);

		return finalScore;
	}
}
