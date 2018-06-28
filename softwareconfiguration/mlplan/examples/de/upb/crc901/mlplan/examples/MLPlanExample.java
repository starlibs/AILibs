package de.upb.crc901.mlplan.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import de.upb.crc901.mlplan.multiclass.DefaultPreorder;
import de.upb.crc901.mlplan.multiclass.MLPlan;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.uncertainty.ISolutionDistanceMetric;
import jaicore.search.algorithms.standard.uncertainty.OversearchAvoidanceConfig;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * This is an example class that illustrates the usage of ML-Plan on
 * the segment dataset of OpenML. It is configured to run for 30 seconds
 * and to use 70% of the data for search and 30% for selection in its second phase.
 * 
 * The API key used for OpenML is ML-Plan's key (read only). 
 * 
 * @author fmohr
 *
 */
public class MLPlanExample {

	public static void main(String[] args) throws Exception {
		
		/* load data for segment dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(40984);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);
		
		/* initialize mlplan, and let it run for 30 seconds */
		int timeoutInSeconds = 30;
		MLPlan mlplan = new MLPlan(new File("model/weka/weka-all-autoweka.json"));
		mlplan.setLoggerName("mlplan");
		mlplan.setTimeout(timeoutInSeconds);
		mlplan.setPortionOfDataForPhase2(.3f);
		mlplan.setNodeEvaluator(new DefaultPreorder());
		OversearchAvoidanceConfig<TFDNode> oversearchAvoidanceConfig = new OversearchAvoidanceConfig<>(OversearchAvoidanceConfig.OversearchAvoidanceMode.TWO_PHASE_SELECTION);
		oversearchAvoidanceConfig.activateDynamicPhaseLengthsAdjustment((long)timeoutInSeconds * 1000000000l);
		oversearchAvoidanceConfig.setSolutionDistanceMetric(new ISolutionDistanceMetric<TFDNode>() {
			
			@Override
			public double calculateSolutionDistance(TFDNode solution1, TFDNode solution2) {
				// TODO Create correct similarity metric
				return 0;
			}
		});
		mlplan.setOversearchAvoidanceConfig(oversearchAvoidanceConfig);
		mlplan.enableVisualization();
		mlplan.buildClassifier(split.get(0));

		/* evaluate solution produced by mlplan */
		Evaluation eval = new Evaluation(split.get(0));
		eval.evaluateModel(mlplan, split.get(1));
		System.out.println("Error Rate of the solution produced by ML-Plan: " + (100 - eval.pctCorrect()) / 100f);
	}
}
