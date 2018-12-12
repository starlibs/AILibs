package de.upb.crc901.mlplan.examples;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import de.upb.crc901.mlplan.multilabel.mekamlplan.ML2PlanMekaBuilder;
import de.upb.crc901.mlplan.multilabel.mekamlplan.ML2PlanMekaClassifier;
import de.upb.crc901.mlplan.multilabel.mekamlplan.meka.MekaML2PlanMekaClassifier;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.multilabel.MultiLabelPerformanceMeasure;
import meka.classifiers.MultiXClassifier;
import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.Metrics;
import meka.core.Result;
import weka.core.Instances;

/**
 * Example demonstrating the usage of Ml2Plan (MLPlan for multilabel
 * classification).
 * 
 * @author Helena Graf
 *
 */
public class ML2PlanARFFExample {

	public static void main(final String[] args) throws Exception {

		/* load data for segment dataset and create a train-test-split */
		Instances data = new Instances(new FileReader("../../../datasets/classification/multi-label/flags.arff"));
		data.setClassIndex(data.numAttributes() - 1);
		Collection<Integer>[] instancesInFolds = WekaUtil.getArbitrarySplit(data, new Random(0), .7f);
		List<Instances> split = WekaUtil.realizeSplit(data, instancesInFolds);

		/* initialize ml2plan, and let it run for 150 seconds */
		ML2PlanMekaClassifier ml2plan = new MekaML2PlanMekaClassifier(new ML2PlanMekaBuilder()
				.withSearchSpaceConfig(new File("conf/automl/searchmodels/meka/mlplan-multilabel.json"))
				.withPerformanceMeasure(MultiLabelPerformanceMeasure.RANK));
		ml2plan.setPortionOfDataForPhase2(0.0f);
		ml2plan.setLoggerName("ml2plan");
		ml2plan.setTimeout(150);
		ml2plan.setTimeoutForNodeEvaluation(60);
		ml2plan.setTimeoutForSingleSolutionEvaluation(60);
		ml2plan.setNumCPUs(2);
		// visualization currently doesn't work
		// mlplan.activateVisualization();

		try {
			long start = System.currentTimeMillis();
			ml2plan.buildClassifier(split.get(0));
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			System.out.println("Finished build of the classifier.");
			System.out
					.println("Chosen model is: " + ((MultiLabelClassifier) ml2plan.getSelectedClassifier()).toString());
			System.out.println("Training time was " + trainTime + "s.");

			/* evaluate solution produced by mlplan */
			Result result = Evaluation.testClassifier((MultiXClassifier) ml2plan, split.get(1));
			double loss = Metrics.L_RankLoss(result.allTrueValues(), result.allPredictions());
			System.out.println("Rank Loss of the solution produced by ML2-Plan: " + loss
					+ ". Internally believed Rank Loss was " + ml2plan.getInternalValidationErrorOfSelectedClassifier());

		} catch (NoSuchElementException e) {
			System.out.println("Building the classifier failed: " + e.getMessage());
		}
	}
}
