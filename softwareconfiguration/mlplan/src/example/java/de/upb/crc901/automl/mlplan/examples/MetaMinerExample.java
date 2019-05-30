package de.upb.crc901.automl.mlplan.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static Logger logger = LoggerFactory.getLogger(MetaMinerExample.class);

	public static void main(String[] args) throws Exception {
		// Load data for a data set and create a train-test-split
		logger.info("Load data.");
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(40984);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data,0, .7f);

		// Initialize meta mlplan and let it run for 2 minutes
		logger.info("Configure ML-Plan");
		MetaMLPlan metaMLPlan = new MetaMLPlan(data);
		metaMLPlan.setCPUs(4);
		metaMLPlan.setTimeOutInSeconds(60);
		metaMLPlan.setMetaFeatureSetName("all");
		metaMLPlan.setDatasetSetName("metaminer_standard");
		// Limit results to 20 pipelines so that the conversion / downloading doesn't take too long
		logger.info("Build meta components");
		StopWatch watch = new StopWatch();
		watch.start();
		metaMLPlan.buildMetaComponents(args[0], args[1], args[2], 5);
		watch.stop();
		logger.info("Find solution");
		metaMLPlan.buildClassifier(split.get(0));

		// Evaluate solution produced by meta mlplan
		logger.info("Evaluate.");
		Evaluation eval = new Evaluation(split.get(0));
		eval.evaluateModel(metaMLPlan, split.get(1));
		logger.info("Error Rate of the solution produced by Meta ML-Plan: {}",(100 - eval.pctCorrect()) / 100f);
		logger.info("Time in Seconds: {}",watch.getTime()/1000);
	}

}