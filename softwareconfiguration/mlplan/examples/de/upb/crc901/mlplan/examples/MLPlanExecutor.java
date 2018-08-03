package de.upb.crc901.mlplan.examples;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import de.upb.crc901.automl.hascoml.HASCOMLTwoPhaseSelection;
import jaicore.ml.WekaUtil;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class MLPlanExecutor {

	public static void main(final String[] args) throws Exception {

		Instances data = new Instances(new FileReader("../../../datasets/classification/multi-class/autos.arff"));
		data.setClassIndex(data.numAttributes() - 1);

		List<Instances> trainTestSplit = WekaUtil.getStratifiedSplit(data, new Random(1), .7);

		HASCOMLTwoPhaseSelection mlPlan = new HASCOMLTwoPhaseSelection(new File("model/weka/weka-all-autoweka.json"));
		mlPlan.setNumberOfCPUs(8);
		mlPlan.setNumberOfConsideredSolutions(100);
		mlPlan.setRandomSeed(0);
		mlPlan.setTimeout(300);
		mlPlan.setTimeoutPerNodeFComputation(15);

		mlPlan.buildClassifier(trainTestSplit.get(0));

		Evaluation eval = new Evaluation(data);
		eval.evaluateModel(mlPlan, trainTestSplit.get(1), new Object[] {});

		System.out.println(eval.errorRate());

	}

}
