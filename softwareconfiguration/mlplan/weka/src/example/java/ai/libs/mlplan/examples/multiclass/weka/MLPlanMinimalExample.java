package ai.libs.mlplan.examples.multiclass.weka;

import java.io.IOException;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.mlplan.weka.MLPlanWekaBuilder;

public class MLPlanMinimalExample {

	public static void main(final String[] args) throws AlgorithmTimeoutedException, AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException, IOException, DatasetDeserializationFailedException {
		IClassifier c = new MLPlanWekaBuilder().withDataset(OpenMLDatasetReader.deserializeDataset(3)).build().call();
	}
}
