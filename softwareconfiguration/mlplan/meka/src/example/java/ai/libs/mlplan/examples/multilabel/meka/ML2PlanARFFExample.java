package ai.libs.mlplan.examples.multilabel.meka;

import java.io.FileReader;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.Timeout;

import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;
import ai.libs.mlplan.multilabel.mekamlplan.MLPlanMekaBuilder;
import meka.core.MLUtils;
import weka.core.Instances;

/**
 * Example demonstrating the usage of Ml2Plan (MLPlan for multilabel classification).
 *
 * @author mwever, helegraf
 *
 */
public class ML2PlanARFFExample {

	private static final boolean ACTIVATE_VISUALIZATION = false;

	public static void main(final String[] args) throws Exception {
		/* load data for segment dataset and create a train-test-split */
		Instances data = new Instances(new FileReader("testrsc/flags.arff"));
		MLUtils.prepareData(data);
		IWekaInstances dataset = new WekaInstances(data);

		List<IWekaInstances> split = WekaUtil.realizeSplit(dataset, WekaUtil.getArbitrarySplit(dataset, new Random(42), 0.7));

		MLPlanClassifierConfig algoConfig = ConfigFactory.create(MLPlanClassifierConfig.class);
		algoConfig.setProperty(MLPlanClassifierConfig.SELECTION_PORTION, "0.3");

		MLPlanMekaBuilder builder = new MLPlanMekaBuilder();
		builder.withAlgorithmConfig(algoConfig);
		builder.withNodeEvaluationTimeOut(new Timeout(60, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new Timeout(60, TimeUnit.SECONDS));
		builder.withNumCpus(4);
		builder.withTimeOut(new Timeout(150, TimeUnit.SECONDS));
		MLPlan<IMekaClassifier> ml2plan = builder.withDataset(new WekaInstances(data)).build();
		ml2plan.setLoggerName("testedalgorithm");
		ml2plan.call();
	}
}