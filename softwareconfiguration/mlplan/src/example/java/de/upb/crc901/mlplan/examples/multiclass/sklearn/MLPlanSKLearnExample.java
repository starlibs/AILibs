package de.upb.crc901.mlplan.examples.multiclass.sklearn;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.MLPlanBuilder;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import de.upb.crc901.mlplan.multiclass.wekamlplan.sklearn.SKLearnMLPlanWekaClassifier;
import jaicore.basic.TimeOut;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import weka.core.Instances;

public class MLPlanSKLearnExample {

	private static final Logger L = LoggerFactory.getLogger(MLPlanSKLearnExample.class);

	private static final File DATASET = new File("testrsc/car.arff");
	private static final ZeroOneLoss LOSS_MEASURE = new ZeroOneLoss();

	private static final TimeOut TIMEOUT = new TimeOut(600, TimeUnit.SECONDS);

	public static void main(final String[] args) throws Exception {
		Instances data = new Instances(new FileReader(DATASET));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> testSplit = WekaUtil.getStratifiedSplit(data, 0, .7);

		MLPlanClassifierConfig mlplanConfig = ConfigFactory.create(MLPlanClassifierConfig.class);

		MLPlanBuilder builder = new MLPlanBuilder();
		builder.withAlgorithmConfig(mlplanConfig);
		builder.withAutoSKLearnConfig();
		builder.withTimeoutForNodeEvaluation(new TimeOut(15, TimeUnit.SECONDS));
		builder.withTimeoutForSingleSolutionEvaluation(new TimeOut(15, TimeUnit.SECONDS));

		SKLearnMLPlanWekaClassifier mlplan = new SKLearnMLPlanWekaClassifier(builder);

		mlplan.setTimeout(TIMEOUT);
		mlplan.setLoggerName("sklmlplanc");
		mlplan.setVisualizationEnabled(true);
		mlplan.buildClassifier(testSplit.get(0));

		List<Double> actual = Arrays.stream(mlplan.classifyInstances(testSplit.get(1))).mapToObj(x -> x).collect(Collectors.toList());
		List<Double> expected = testSplit.get(1).stream().map(x -> x.classValue()).collect(Collectors.toList());
		double loss = LOSS_MEASURE.calculateAvgMeasure(actual, expected);
		L.info("ML-Plan classifier has been chosen for dataset {} and framework SK-Learn. The measured test loss of the selected classifier is {}", DATASET.getAbsolutePath(), loss);
	}

}
