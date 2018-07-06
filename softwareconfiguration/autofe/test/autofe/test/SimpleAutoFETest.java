package autofe.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.HASCOFE;
import autofe.algorithm.hasco.HASCOFE.HASCOFESolution;
import autofe.algorithm.hasco.evaluation.ClusterNodeEvaluator;
import autofe.algorithm.hasco.evaluation.ClusterObjectEvaluator;
import autofe.util.DataSet;
import jaicore.ml.WekaUtil;
import weka.core.Instances;

public class SimpleAutoFETest extends AutoFETest {
	private static final Logger logger = LoggerFactory.getLogger(SimpleAutoFETest.class);

	@Test
	public void testHASCO() throws Exception {
		logger.info("Starting AutoFE test...");

		/* load data for segment dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(SEGMENT_ID);
		File file = ds.getDataset(API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);

		HASCOFE hascoFE = new HASCOFE(new File("model/test.json"), new ClusterNodeEvaluator(20),
				new DataSet(split.get(0), null), new ClusterObjectEvaluator());
		hascoFE.setLoggerName("autofe");
		// hascoFE.enableVisualization();
		hascoFE.runSearch(120 * 1000);
		HASCOFESolution solution = hascoFE.getCurrentlyBestSolution();

		logger.info(hascoFE.getFoundClassifiers().toString());
		logger.info(solution.toString());
	}
}
