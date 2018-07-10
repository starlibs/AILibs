package autofe.algorithm.hasco.evaluation;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.EvaluationUtils;
import jaicore.ml.WekaUtil;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LDA;
import weka.core.Instances;

public class LDAObjectEvaluator extends AbstractHASCOFEObjectEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(LDAObjectEvaluator.class);

	@Override
	public Double evaluate(FilterPipeline pipeline) throws Exception {
		logger.debug("Applying and evaluating pipeline " + pipeline.toString());
		DataSet dataSet = pipeline.applyFilter(this.data, true);

		List<Instances> split = WekaUtil.getStratifiedSplit(dataSet.getInstances(), new Random(42), .7f);

		// TODO: Perform LDA
		LDA lda = new LDA();
		// FLDA lda = new FLDA();
		lda.buildClassifier(split.get(0));

		Evaluation eval = new Evaluation(split.get(0));
		eval.evaluateModel(lda, split.get(1));
		logger.debug("LDA object evaluator pct correct: " + eval.pctCorrect());
		return 1 - (eval.pctCorrect() / 100.0)
				+ ATT_COUNT_PENALTY * EvaluationUtils.calculateAttributeCountPenalty(this.data.getInstances());
	}

}
