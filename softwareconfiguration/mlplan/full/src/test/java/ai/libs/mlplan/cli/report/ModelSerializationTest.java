package ai.libs.mlplan.cli.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.ml.core.dataset.FileDatasetDescriptor;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.mlplan.cli.MLPlanCLI;

class ModelSerializationTest {

	@Test
	void testThatModelIsSerializedAndCanBeRecoveredForPredictions() throws Exception {

		File modelFile = new File("tmp/testmodel.mod");
		if (modelFile.exists()) {
			modelFile.delete();
		}

		/* configure run */
		File arffFile = new File("../../../JAICore/jaicore-ml-weka/testrsc/dataset/arff/breast.cancer.arff");
		String[] mainArgs = new String[] {"-f", arffFile.getAbsolutePath(), "-t", "20", "-om", modelFile.getAbsolutePath()};

		/* execute run */
		MLPlanCLI.main(mainArgs);

		/* check model existence and usability */
		assertTrue(modelFile.exists());
		IWekaClassifier classifier = (IWekaClassifier)FileUtil.unserializeObject(modelFile.getAbsolutePath());
		ILabeledDataset<ILabeledInstance> ds = new ArffDatasetAdapter().deserializeDataset(new FileDatasetDescriptor(arffFile), "Class");
		IPredictionBatch predictions = classifier.predict(ds);
		assertEquals(ds.size(), predictions.getPredictions().size());
	}

}
