package ai.libs.mlplan.wekamlplan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.GraphGeneratorTester;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.weka.MLPlanWekaBuilder;

public class MLPlanGraphGeneratorTest extends GraphGeneratorTester<TFDNode, String> {

	public static Stream<Arguments> getGraphGenerators() throws IOException, DatasetDeserializationFailedException {

		/* extract graph generator from mlplan */
		WekaInstances data = new WekaInstances(ArffDatasetAdapter.readDataset(new File("testrsc/car.arff")));
		MLPlan<IWekaClassifier> mlplan = new MLPlanWekaBuilder().withDataset(data).build();
		mlplan.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		IGraphGenerator<TFDNode, String> graphGenerator = mlplan.getSearchProblemInputGenerator().getGraphGenerator();

		/* generate the actual input for the test */
		List<Arguments> gg = new ArrayList<>();
		gg.add(Arguments.of("Weka ML-Plan", graphGenerator));
		return gg.stream();
	}

}
