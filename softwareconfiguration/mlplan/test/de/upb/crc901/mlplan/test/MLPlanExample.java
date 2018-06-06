package de.upb.crc901.mlplan.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import de.upb.crc901.mlplan.multiclass.DefaultPreorder;
import de.upb.crc901.mlplan.multiclass.MLPlan;
import jaicore.ml.WekaUtil;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class MLPlanExample {

//	@Test
	public void test() throws Exception {
		
		/* load data for segment dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(40984);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);

		/* initialize mlplan, and let it run for 10 seconds */
		MLPlan mlplan = new MLPlan(new File("model/weka/weka-all-autoweka.json"));
		mlplan.setTimeout(10);
		mlplan.setPortionOfDataForPhase2(.3f);
		mlplan.setNodeEvaluator(new DefaultPreorder());
		mlplan.enableVisualization();
		mlplan.buildClassifier(split.get(0));

		/* evaluate solution produced by mlplan */
		Evaluation eval = new Evaluation(split.get(0));
		eval.evaluateModel(mlplan, split.get(1));
		System.out.println("Error Rate: " + (100 - eval.pctCorrect()) / 100f);
	}
}
