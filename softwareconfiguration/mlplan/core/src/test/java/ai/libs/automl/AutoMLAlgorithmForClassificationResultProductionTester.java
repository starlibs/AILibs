package ai.libs.automl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.DatasetUtil;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.experiments.OpenMLProblemSet;

public abstract class AutoMLAlgorithmForClassificationResultProductionTester extends AutoMLAlgorithmResultProductionTester {

	@BeforeEach
	@Timeout(value = 180, unit = TimeUnit.SECONDS)
	void setUp() {
		// nothing to do here
	}

	@Override
	public IDeterministicPredictionPerformanceMeasure<?, ?> getTestMeasure() {
		return EClassificationPerformanceMeasure.ERRORRATE;
	}

	@Override
	public List<ILabeledDataset<?>> getTrainTestSplit(final ILabeledDataset<?> dataset) throws SplitFailedException, InterruptedException {
		ILabeledDataset<?> myDataset = dataset;
		if (myDataset.getLabelAttribute() instanceof INumericAttribute) {
			this.logger.info("Changing regression dataset to classification dataset!");
			myDataset = DatasetUtil.convertToClassificationDataset(myDataset);
		}
		return SplitterUtil.getLabelStratifiedTrainTestSplit(myDataset, new Random(0), .7);
	}

	public static Stream<Arguments> getDatasets() throws DatasetDeserializationFailedException {
		try {
			List<OpenMLProblemSet> problemSets = new ArrayList<>();
			problemSets.add(new OpenMLProblemSet(1101)); // lymphoma_2classes
			problemSets.add(new OpenMLProblemSet(3)); // kr-vs-kp
			problemSets.add(new OpenMLProblemSet(9)); // autos
			problemSets.add(new OpenMLProblemSet(24)); // mushroom
			problemSets.add(new OpenMLProblemSet(39)); // ecoli
			problemSets.add(new OpenMLProblemSet(44)); // spambase
			problemSets.add(new OpenMLProblemSet(60)); // waveform-5000
			problemSets.add(new OpenMLProblemSet(61)); // iris
			// problemSets.add(new OpenMLProblemSet(149)); // CovP okElec
			problemSets.add(new OpenMLProblemSet(155)); // pokerhand
			problemSets.add(new OpenMLProblemSet(182)); // satimage
			problemSets.add(new OpenMLProblemSet(273)); // IMDB drama
			problemSets.add(new OpenMLProblemSet(554)); // mnist
			problemSets.add(new OpenMLProblemSet(1039)); // hiva-agnostic
			problemSets.add(new OpenMLProblemSet(1104)); // leukemia
			problemSets.add(new OpenMLProblemSet(1150)); // AP_Breast_Lung
			problemSets.add(new OpenMLProblemSet(1152)); // AP_Prostate_Ovary
			problemSets.add(new OpenMLProblemSet(1156)); // AP_Omentum_Ovary
			// problemSets.add(new OpenMLProblemSet(1240)); // AirlinesCodrnaAdult
			problemSets.add(new OpenMLProblemSet(1457)); // amazon
			problemSets.add(new OpenMLProblemSet(1501)); // semeion
			// problemSets.add(new OpenMLProblemSet(1590)); // adult # THIS ARFF CANNOT BE PARSED BY THE PYTHON ARFF LOADERS
			problemSets.add(new OpenMLProblemSet(1590)); // adult # THIS ARFF CANNOT BE PARSED BY THE PYTHON ARFF LOADERS
			problemSets.add(new OpenMLProblemSet(4136)); // dexter
			//			problemSets.add(new OpenMLProblemSet(4137)); // dorothea // TEMPORARILY DISABLED since problems with data loading procedure
			problemSets.add(new OpenMLProblemSet(40668)); // connect-4
			problemSets.add(new OpenMLProblemSet(40691)); // winequality
			// problemSets.add(new OpenMLProblemSet(40927)); // cifar-10
			// problemSets.add(new OpenMLProblemSet(41026)); // gisette
			// problemSets.add(new OpenMLProblemSet(41065)); // mnist-rotate
			problemSets.add(new OpenMLProblemSet(41066)); // secom
			// problemSets.add(new OpenMLProblemSet(41705)); // ASP-POTASSCO-classification # THIS ARFF CANNOT BE PARSED BY THE PYTHON ARFF LOADERS
			return problemSets.stream().map(Arguments::of);
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException(e);
		}
	}

}
