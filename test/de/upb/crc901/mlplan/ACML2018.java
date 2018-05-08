package de.upb.crc901.mlplan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import de.upb.crc901.automl.hascowekaml.HASCOForMEKA;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.structure.core.Node;
import meka.core.MLUtils;
import weka.core.Instances;

public class ACML2018 {

	@Test
	public void test() throws Exception {
		
		/* read data and split */
		Instances data = new Instances(new BufferedReader(new FileReader(new File("../ML-Plan/testrsc/multilabel/flags.arff"))));
		Collections.shuffle(data);
		try {
			MLUtils.prepareData(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Instances> split = WekaUtil.realizeSplit(data, WekaUtil.getArbitrarySplit(data, new Random(0), .7f));
		
		
		HASCOForMEKA hasco = new HASCOForMEKA();
//		mlplan.registerListener(new HASCOSQLEventLogger<>(new MySQLAdapter("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "hasco")));
//		mlplan.registerListener(new HASCOSQLEventLogger<>(new MySQLAdapter("hsqldb:hsql", "localhost", "SA", "", "testdb", new Properties())));
		
		
		new SimpleGraphVisualizationWindow<Node<TFDNode,Double>>(hasco).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());
//		hasco.setPreferredNodeEvaluator(n -> n.externalPath().size() * 1.0);
		
		
		hasco.setNumberOfCPUs(8);
		hasco.gatherSolutions(split.get(0), 60 * 1000 * 1);
//		HASCOForMEKASolution bestSolution = hasco.getCurrentlyBestSolution();
//		System.out.println("Best solution: " + bestSolution.getClassifier().getClass().getName() + Arrays.toString(bestSolution.getClassifier().getOptions()) + " with base learner " + ((SingleClassifierEnhancer)bestSolution.getClassifier()).getClassifier().getClass().getName() + ". Score: " + bestSolution.getScore());
		
//		MultilabelEvaluator eval = new F1AverageMultilabelEvaluator(new Random(0));
//		double f1 = eval.getErrorRateForSplit(bestSolution.getClassifier(), split.get(0), split.get(1));
//		System.out.println("External f1 error of this solution: " + f1);
	}

}
