package ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.aeonbits.owner.ConfigCache;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner.BOSSClassifier;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner.BOSSLearningAlgorithm.IBossAlgorithmConfig;

/**
 * @author Helen DFT JUnit test
 *
 */
public class BOSSAlgorithmTest {

	private TimeSeriesDataset2 dataset;

	@Before
	public void setup() {
		double[] timeseries1 = new double[] { 1, 1, 1, 1, 1, 1, 1, 1 };
		double[] timeseries2 = new double[] { 1, 2, 4, 3, 5, 2, 4, 3 };
		double[][] matrix = new double[3][8];
		matrix[0] = timeseries1;
		matrix[1] = timeseries2;

		List<double[][]> futureDataSet = new ArrayList<>();
		futureDataSet.add(matrix);
		this.dataset = new TimeSeriesDataset2(futureDataSet, null, null);
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
		fail("This is just a reminder that something should be tested here, which is currently not the case!");
	}

}
