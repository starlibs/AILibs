package jaicore.ml.tsc.classifier;

import java.util.ArrayList;

import org.aeonbits.owner.ConfigCache;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.classifier.BOSSLearningAlgorithm.IBossAlgorithmConfig;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

/**
 * @author Helen
 * DFT JUnit test
 *
 */
@RunWith(JUnit4.class)
public class BOSSAlgorithmTest {
	double[] timeseries1;
	double[] timeseries2;

	TimeSeriesDataset dataset;

	@Before
	public void setup() {
		this.timeseries1 = new double [] {1,1,1,1,1,1,1,1};
		this.timeseries2 = new double[] {1,2,4,3,5,2,4,3};
		double[][] matrix = new double[3][8];
		matrix[0] = this.timeseries1;
		matrix[1] = this.timeseries2;

		ArrayList<double[][]> futureDataSet = new ArrayList<double[][]>();
		futureDataSet.add(matrix);
		this.dataset = new TimeSeriesDataset(futureDataSet,null, null);
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFit() throws TrainingException {
		IBossAlgorithmConfig config = ConfigCache.getOrCreate(IBossAlgorithmConfig.class);
		config.setProperty(IBossAlgorithmConfig.K_WINDOW_SIZE, "" + 3);
		config.setProperty(IBossAlgorithmConfig.K_WORDLENGTH, "" + 3);
		config.setProperty(IBossAlgorithmConfig.K_ALPHABET, "1.0,2.0,3.0");
		config.setProperty(IBossAlgorithmConfig.K_MEANCORRECTED, "" + false);
		BOSSClassifier test2 = new BOSSClassifier(config);
		test2.train(this.dataset);
	}

}
