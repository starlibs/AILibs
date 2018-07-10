package autofe.algorithm.hasco.evaluation;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.EvaluationUtils;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.structure.core.Node;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

public class EnsembleNodeEvaluator extends AbstractHASCOFENodeEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(EnsembleNodeEvaluator.class);

	public EnsembleNodeEvaluator(final int maxPipelineSize) {
		super(maxPipelineSize);
	}

	@Override
	public Double f(Node<TFDNode, ?> node) throws Throwable {
		if (this.getHascoFE() == null)
			throw new IllegalStateException("HascoFE property of the cluster node evaluator must be initialized!");

		if (node.getParent() == null)
			return null;

		// If pipeline is too deep, assign worst value
		if (node.path().size() > this.maxPipelineSize)
			return MAX_EVAL_VALUE;

		FilterPipeline pipe = this.getPipelineFromNode(node);

		if (pipe != null && pipe.getFilters() != null) {
			try {
				logger.debug("Applying and evaluating pipeline " + pipe.toString());
				DataSet dataSet = pipe.applyFilter(this.data, true);
				logger.debug("Applied pipeline. Starting benchmarking...");

				// TODO
				/* Relief */
				ReliefFAttributeEval relief = new ReliefFAttributeEval();
				relief.buildEvaluator(dataSet.getInstances());
				double attEvalSum = 0;
				for (int i = 0; i < dataSet.getInstances().numAttributes() - 1; i++) {
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

				double finalScore = Math.min(
						1 - (0.33 * attEvalSum + 0.33 * knnResult + 0.33 * varianceMean)
								+ ATT_COUNT_PENALTY
										* EvaluationUtils.calculateAttributeCountPenalty(this.data.getInstances()),
						19999.0);

				logger.info("Ensemble result: " + finalScore);

				return finalScore;

			} catch (Exception e) {
				e.printStackTrace();
				logger.warn("Could not evaluate pipeline. Reason: " + e.getMessage());
				return null;
			}
		} else {
			logger.debug("Null pipe");
			return null;
		}
	}

}
