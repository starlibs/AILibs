package ai.libs.automl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.Study;

import ai.libs.jaicore.ml.core.dataset.DatasetUtil;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.experiments.OpenMLProblemSet;
import ai.libs.jaicore.ml.regression.loss.ERegressionPerformanceMeasure;

public abstract class AutoMLAlgorithmForRegressionResultProductionTest extends AutoMLAlgorithmResultProductionTester {

	private static OpenmlConnector con = new OpenmlConnector();

	@Override
	public IDeterministicPredictionPerformanceMeasure<?, ?> getTestMeasure() {
		return ERegressionPerformanceMeasure.RMSE;
	}

	@Override
	public List<ILabeledDataset<?>> getTrainTestSplit(final ILabeledDataset<?> dataset) throws SplitFailedException, InterruptedException {
		ILabeledDataset<?> myDataset = dataset;
		if (myDataset.getLabelAttribute() instanceof ICategoricalAttribute) {
			this.logger.info("Changing classification dataset to regression dataset!");
			myDataset = DatasetUtil.convertToRegressionDataset(myDataset);
		}
		return SplitterUtil.getSimpleTrainTestSplit(myDataset, new Random(0), .7);
	}

	// creates the test data
	public static Stream<OpenMLProblemSet> getDatasets() throws DatasetDeserializationFailedException {
		try {
			List<Integer> ignoreDatasets = Arrays.asList(565);
			List<OpenMLProblemSet> problemSets = new ArrayList<>();
			Study study = con.studyGet(130); // openml regression 30
			Arrays.stream(study.getDataset()).map(x -> {
				if (ignoreDatasets.contains(x)) {
					return null;
				}
				try {
					return new OpenMLProblemSet(x);
				} catch (Exception e) {
					return null;
				}
			}).filter(x -> x != null).forEach(problemSets::add);
			return problemSets.stream();
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException(e);
		}
	}

}
