package autofe.algorithm.hasco.evaluation;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.EvaluationUtils;
import jaicore.ml.WekaUtil;
import weka.core.Instances;

public class COCOObjectEvaluator extends AbstractHASCOFEObjectEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(COCOObjectEvaluator.class);

	@Override
	public Double evaluate(FilterPipeline object) throws Exception {

		if (object == null)
			return 20000d;

		logger.debug("Applying and evaluating pipeline " + object.toString());
		DataSet dataSet = object.applyFilter(this.data, true);

		logger.debug("Applied pipeline. Starting benchmarking...");

		List<Instances> split = WekaUtil.getStratifiedSplit(dataSet.getInstances(), new Random(42), 0.01d);
		Instances insts = split.get(0);

		double loss = EvaluationUtils.calculateCOCOForBatch(insts);

		logger.debug("COCO object evaluation score: " + loss);
		return loss;
	}
}
