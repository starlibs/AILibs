package de.upb.crc901.automl.mlplan.examples;
//package de.upb.crc901.mlplan.examples;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.util.List;
//import java.util.Random;
//
//import org.openml.apiconnector.io.OpenmlConnector;
//import org.openml.apiconnector.xml.DataSetDescription;
//
//import de.upb.crc901.automl.hascoml.supervised.multiclass.weka.HASCOForWekaML;
//import hasco.reduction.HASCOReduction;
//import hasco.serialization.ComponentLoader;
//import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
//import jaicore.ml.WekaUtil;
//import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionHTNPlannerFactory;
//import jaicore.planning.graphgenerators.task.tfd.TFDNode;
//import jaicore.search.algorithms.standard.bestfirst.BestFirst;
//import jaicore.search.core.interfaces.GraphGenerator;
//import weka.core.Instances;
//
///**
// * Illustrates the usage of the WEKAMetaMiner.
// *
// * @author Helena Graf
// *
// */
//public class MetaMinerExample {
//
//	public static void main(final String[] args) throws Exception {
//		/* load data for segment dataset and create a train-test-split */
//		OpenmlConnector connector = new OpenmlConnector();
//		DataSetDescription ds = connector.dataGet(40983);
//		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
//		Instances data = new Instances(new BufferedReader(new FileReader(file)));
//		data.setClassIndex(data.numAttributes() - 1);
//		List<Instances> instances = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);
//
//		/* initialize mlplan, and let it run for 30 seconds */
//		File configFile = new File("model/weka/weka-all-autoweka.json");
//		HASCOForWekaML hasco = new HASCOForWekaML();
//		ComponentLoader componentLoader = hasco.getComponentLoader();
//
//		/* get the graph generator from the reduction */
//		HASCOReduction reduction = new HASCOReduction(configFile, "AbstractClassifier", true);
//		GraphGenerator<TFDNode, String> graphGenerator = reduction.getGraphGeneratorUsedByHASCOForSpecificPlanner(new ForwardDecompositionHTNPlannerFactory<Double>());
//		BestFirst<TFDNode, String> bf = new BestFirst<>(graphGenerator, n -> n.externalPath().size() * -1.0);
//		new SimpleGraphVisualizationWindow<>(bf);
//		while (true) {
//			bf.nextSolution();
//		}
//
//		// System.out.println(hasco.getGraphGenerator());
//
//		// WEKAMetaminer metaMiner = new WEKAMetaminer(data);
//		// metaMiner.build();
//		// MetaMinerBasedSorter comparator = new MetaMinerBasedSorter(metaMiner,
//		// componentLoader);
//		// mlplan.get.setOrGraphSearchFactory(new
//		// ImprovedLimitedDiscrepancySearchFactory(comparator));
//
//		// mlplan.buildClassifier(split.get(0));
//
//		/* evaluate solution produced by mlplan */
//		// Evaluation eval = new Evaluation(split.get(0));
//		// eval.evaluateModel(mlplan, split.get(1));
//		// System.out.println("Error Rate of the solution produced by ML-Plan: " + (100
//		// - eval.pctCorrect()) / 100f);
//	}
//
//}