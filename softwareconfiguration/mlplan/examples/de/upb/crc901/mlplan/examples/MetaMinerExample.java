package de.upb.crc901.mlplan.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.time.StopWatch;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import de.upb.crc901.mlplan.metamining.MetaMLPlan;
import jaicore.ml.WekaUtil;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * Illustrates the usage of the WEKAMetaMiner.
 * 
 * @author Helena Graf
 *
 */
public class MetaMinerExample {

	public static void main(String[] args) throws Exception {
		// Load data for a data set and create a train-test-split
		System.out.println("Example: Load data.");
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(40984);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);

		// Initialize meta mlplan and let it run for 2 minutes
		System.out.println("Example: Configure ML-Plan");
		MetaMLPlan metaMLPlan = new MetaMLPlan(data);
		metaMLPlan.setCPUs(4);
		metaMLPlan.setTimeOutInSeconds(60000);
		metaMLPlan.setMetaFeatureSetName("all");
		metaMLPlan.setDatasetSetName("metaminer_standard");
		// Limit results to 20 pipelines so that the conversion / downloading doesn't take too long
		System.out.println("Example: build meta components");
		StopWatch watch = new StopWatch();
		watch.start();
		metaMLPlan.buildMetaComponents(args[0], args[1], args[2], 20);
		watch.stop();
		System.out.println("Example: find solution");
		metaMLPlan.buildClassifier(split.get(0));

		// Evaluate solution produced by meta mlplan
		System.out.println("Example: ");
		Evaluation eval = new Evaluation(split.get(0));
		eval.evaluateModel(metaMLPlan, split.get(1));
		System.out.println("Error Rate of the solution produced by Meta ML-Plan: " + (100 - eval.pctCorrect()) / 100f);
		System.out.println(watch.getTime());
	}

}
