package ai.libs.jaicore.ml.core.dataset.serialization;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.ml.experiments.OpenMLProblemSet;

public class CSVDatasetAdapterTest {

	// creates the test data
	public static Stream<Arguments> readDatasets(final List<Integer> ids) {
		return ids.stream().map(t -> {
			try {
				return Arguments.of(new OpenMLProblemSet(t));
			} catch (Exception e) {
				e.printStackTrace();
				throw new IllegalStateException(e);
			}
		});
	}

	public static Stream<Arguments> getDatasets() throws IOException, Exception {
		return readDatasets(Arrays.asList(3, // kr-vs-kp
				6, // letter
				9, // autos
				12, // mfeat-factors
				14, // mfeat-fourier
				16, // mfeat-karhunen
				18, // mfeat-morph
				21, // car
				22, // mfeat-zernike
				23, // cmc
				24, // mushroom
				26, // nursey
				28, // optdigits
				30, // page-blocks
				31, // credit-g
				32, // pendigits
				36, // segment
				38, // sick
				39, // ecoli
				44, // spambase
				// /* */ 46 // splice => contains ignore attribute which we cannot deal with yet.
				57, // hypothyroid
				60, // waveform-5000; this dataset has a -0.0 entry and can hence not be recovered appropriately
				61, // iris
				179, // adult
				181, // yeast
				182, // satimage
				183, // abalone
				184, // kropt
				185, // baseball
				188,
				1101, // lymphoma_2classes
				1104, // leukemia
				1501, // semeion
				1515, // micro-mass
				1590, // adult
				40691, // wine-quality-red
				41066, // secom
				180, // covertype
				273, // IMDB Drama
				293, // covertype
				300, // isolet
				351, // codrna
				354, // poker
				//				554, // MNIST
				1039, // hiva_agnostic
				1111,
				1150, // AP_Breast_Lung
				1152, // AP_Prostate_Ovary
				1156, // AP_Omentum_Ovary
				1457, // amazon
				1461,
				1486,
				1590,
				4534,
				4135,
				4541,
				40670,
				40701,
				40978,
				40981,
				41162,
				41143,
				41147,
				40975,
				31,
				4136, // dexter
				4137, // dorothea
				23512, // higgs
				// /**/ 40594, // Reuters => Multi target
				40668, // connect-4
				41064 // convex
				// /**/ 42123 // articleinfluence => string attribute
				));
	}

	@ParameterizedTest(name="test CSV serializability of openml id {0}")
	@MethodSource("getDatasets")
	public void testWriteDataset(final OpenMLProblemSet problemset) throws DatasetDeserializationFailedException, IOException {
		ILabeledDataset<ILabeledInstance> dataset = OpenMLDatasetReader.deserializeDataset(problemset.getId());
		File f = new File("test.csv");
		CSVDatasetAdapter.writeDataset(f, dataset);
		assertTrue(f.exists());
	}

}
