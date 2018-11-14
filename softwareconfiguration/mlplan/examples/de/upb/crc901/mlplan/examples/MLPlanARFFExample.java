package de.upb.crc901.mlplan.examples;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaMLPlanWekaClassifier;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import jaicore.ml.WekaUtil;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class MLPlanARFFExample {

	public static void main(final String[] args) throws Exception {

		/* load data for segment dataset and create a train-test-split */
		Instances data = new Instances(new FileReader("../../../../../datasets/classification/multi-class/car.arff"));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);

		/* initialize mlplan with a tiny search space, and let it run for 30 seconds */
		MLPlanWekaClassifier mlplan = new WekaMLPlanWekaClassifier(new MLPlanWekaBuilder().withSearchSpaceConfigFile(new File("conf/automl/searchmodels/weka/tinytest.json")));
		mlplan.setPortionOfDataForPhase2(0.3f);
		mlplan.setLoggerName("mlplan");
		mlplan.setTimeout(300);
		mlplan.setTimeoutForNodeEvaluation(15);
		mlplan.setTimeoutForSingleSolutionEvaluation(15);
		mlplan.setNumCPUs(8);
		mlplan.activateVisualization();
		try {
			long start = System.currentTimeMillis();
			mlplan.buildClassifier(split.get(0));
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			System.out.println("Finished build of the classifier.");
			System.out.println("Chosen model is: " + ((MLPipeline)mlplan.getSelectedClassifier()).toString());
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
