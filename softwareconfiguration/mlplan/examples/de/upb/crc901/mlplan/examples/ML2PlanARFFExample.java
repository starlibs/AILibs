package de.upb.crc901.mlplan.examples;

import java.io.FileReader;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import de.upb.crc901.mlplan.multilabel.ML2PlanMekaClassifier;
import de.upb.crc901.mlplan.multilabel.MekaML2PlanMekaClassifier;
import de.upb.crc901.mlplan.multilabel.MultilabelMLPipeline;
import jaicore.ml.WekaUtil;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class Ml2PlanARFFExample {
	public static void main(final String[] args) throws Exception {

		/* load data for segment dataset and create a train-test-split */
		Instances data = new Instances(new FileReader("../../../datasets/classification/multi-label/flags.arff"));
		data.setClassIndex(data.numAttributes() - 1);
		Collection<Integer>[] instancesInFolds = WekaUtil.getArbitrarySplit(data,
				new Random(0), .7f);
		List<Instances> split = WekaUtil.realizeSplit(data, instancesInFolds);

		/* initialize mlplan with a tiny search space, and let it run for 30 seconds */
		//ML2PlanMekaClassifier mlplan = new MekaML2PlanMekaClassifier(new ML2PlanMekaBuilder().withSearchSpaceConfig(new File("conf/automl/searchmodels/meka/meka-multilabel-small.json")));
		ML2PlanMekaClassifier mlplan = new MekaML2PlanMekaClassifier();
		mlplan.setPortionOfDataForPhase2(0.0f);
		mlplan.setLoggerName("ml2plan");
		mlplan.setTimeout(30);
		mlplan.setTimeoutForNodeEvaluation(15);
		mlplan.setTimeoutForSingleSolutionEvaluation(15);
		mlplan.setNumCPUs(1);
		//mlplan.activateVisualization();
		try {
			long start = System.currentTimeMillis();
			mlplan.buildClassifier(split.get(0));
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			System.out.println("Finished build of the classifier.");
			System.out.println("Chosen model is: " + ((MultilabelMLPipeline)mlplan.getSelectedClassifier()).toString());
			System.out.println("Training time was " + trainTime + "s.");
			
			/* evaluate solution produced by mlplan */
			Evaluation eval = new Evaluation(split.get(0));
			eval.evaluateModel(mlplan, split.get(1));
			System.out.println("Error Rate of the solution produced by ML-Plan: " + ((100 - eval.pctCorrect()) / 100f) + ". Internally believed error was " + mlplan.getInternalValidationErrorOfSelectedClassifier());
		} catch (NoSuchElementException e) {
			System.out.println("Building the classifier failed: " + e.getMessage());
		}
	}
}
