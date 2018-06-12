package autofe.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import autofe.algorithm.hasco.HASCOFE;
import autofe.algorithm.hasco.HASCOFE.HASCOFESolution;
import autofe.algorithm.hasco.evaluation.ClusterEvaluator;
import jaicore.ml.WekaUtil;
import weka.core.Instances;

public class SimpleAutoFETest {

	@Test
	public void testHASCO() throws Exception {
		System.out.println("Starting AutoFE test...");

		/* load data for segment dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(40984);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);

		HASCOFE<Instances> hascoFE = new HASCOFE<>(new File("model/test.json"), n -> null, split.get(0),
				new ClusterEvaluator<>());
		hascoFE.setLoggerName("autofe");
		hascoFE.enableVisualization();
		hascoFE.runSearch(10 * 1000);
		HASCOFESolution solution = hascoFE.getCurrentlyBestSolution();
		System.out.println(solution);
		System.out.println(hascoFE.getFoundClassifiers());
		// System.out.println(solution.getSolution().toString());
	}
}
