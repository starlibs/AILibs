package de.upb.crc901.mlplan.test;

import java.io.FileReader;
import java.util.List;
import java.util.Random;

import de.upb.crc901.mlplan.multiclass.MLPlanWEKAClassifier;
import jaicore.ml.WekaUtil;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class MLPlanWEKAClassifierTest {

	public static void main(final String[] args) throws Exception {
		Instances data = new Instances(new FileReader("../../../datasets/classification/multi-class/ecoli.arff"));
		data.setClassIndex(data.numAttributes() - 1);

		List<Instances> dataSplit = WekaUtil.getStratifiedSplit(data, new Random(1), .7f);

		MLPlanWEKAClassifier mlplan = new MLPlanWEKAClassifier();
		mlplan.enableVisualization();
		mlplan.buildClassifier(data);

		Evaluation eval = new Evaluation(dataSplit.get(0));
		eval.evaluateModel(mlplan, dataSplit.get(1), new Object[] {});

		System.out.println("Error Rate: " + eval.errorRate());
	}

}
