package de.upb.crc901.mlplan.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import de.upb.crc901.automl.hascowekaml.HASCOForWekaML;
import de.upb.crc901.automl.metamining.WEKAMetaminer;
import de.upb.crc901.mlplan.multiclass.DefaultPreorder;
import de.upb.crc901.mlplan.multiclass.MLPlan;
import hasco.metamining.MetaMinerBasedSorter;
import hasco.metamining.factories.ImprovedLimitedDiscrepancySearchFactory;
import hasco.serialization.ComponentLoader;
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
		/* load data for segment dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(40983);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);
		
		/* initialize mlplan, and let it run for 30 seconds */
		File configFile = new File("model/weka/weka-all-autoweka.json");
		HASCOForWekaML hasco = new HASCOForWekaML(configFile);
		ComponentLoader componentLoader = new ComponentLoader();
		componentLoader.loadComponents(configFile);

		hasco.getGraphGenerator();
		
//		WEKAMetaminer metaMiner = new WEKAMetaminer(data);
//		metaMiner.build();
//		MetaMinerBasedSorter comparator = new MetaMinerBasedSorter(metaMiner, componentLoader);
//		mlplan.get.setOrGraphSearchFactory(new ImprovedLimitedDiscrepancySearchFactory(comparator));

//		mlplan.buildClassifier(split.get(0));

		/* evaluate solution produced by mlplan */
//		Evaluation eval = new Evaluation(split.get(0));
//		eval.evaluateModel(mlplan, split.get(1));
//		System.out.println("Error Rate of the solution produced by ML-Plan: " + (100 - eval.pctCorrect()) / 100f);
	}

}
