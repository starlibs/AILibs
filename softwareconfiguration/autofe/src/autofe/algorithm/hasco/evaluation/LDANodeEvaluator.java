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
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LDA;
import weka.core.Instances;

public class LDANodeEvaluator extends AbstractHASCOFENodeEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(LDANodeEvaluator.class);

	public LDANodeEvaluator(final int maxPipelineSize) {
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

				List<Instances> split = WekaUtil.getStratifiedSplit(dataSet.getInstances(), new Random(42), .7f);

				// TODO: Perform LDA
				LDA lda = new LDA();
				// FLDA lda = new FLDA();
				lda.buildClassifier(split.get(0));

				Evaluation eval = new Evaluation(split.get(0));
				eval.evaluateModel(lda, split.get(1));

				double finalScore = Math.min(
						1 - (eval.pctCorrect() / 100.0)
								+ ATT_COUNT_PENALTY
										* EvaluationUtils.calculateAttributeCountPenalty(this.data.getInstances()),
						19999.0);

				logger.debug("Final LDA score: " + finalScore);
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
