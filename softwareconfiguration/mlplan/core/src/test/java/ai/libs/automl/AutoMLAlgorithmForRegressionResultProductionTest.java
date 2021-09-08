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
			List<OpenMLProblemSet> problemSets = new ArrayList<>();
			Study study = con.studyGet(130); // openml regression 30
			List<Integer> datasetIDs = Arrays.asList(// list of openml ids
					495, // baseball-pitcher
					41021, // moneyball
					574, // house_16H
					564, // fried
					558, // bank32nh
					550, // quake
					549, // strikes
					547, // no2
					546, // sensory
					541, // socmob
					537, // houses
					531, // boston
					528, // humandevel
					512, // balloon
					507, // space_ga
					505, // tecator
					503, // wind
					497, // veteran
					405, // mtp
					344, // mv
					308, // puma32H
					287, // wine_quality
					227, // cpu_small
					223, // stock
					216, // elevators
					215, // 2dplanes
					196, // autoMpg
					189, // kin8nm
					183 // abalone
			);

			datasetIDs.stream().map(x -> {
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
