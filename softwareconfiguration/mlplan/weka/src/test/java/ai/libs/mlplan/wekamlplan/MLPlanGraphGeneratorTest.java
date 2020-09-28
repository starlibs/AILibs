package ai.libs.mlplan.wekamlplan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.GraphGeneratorTester;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;

public class MLPlanGraphGeneratorTest extends GraphGeneratorTester<TFDNode, String> {

	@Override
	public List<Pair<IGraphGenerator<TFDNode, String>, Integer>> getGraphGenerators() throws IOException, DatasetDeserializationFailedException {

		/* extract graph generator from mlplan */
		WekaInstances data = new WekaInstances(ArffDatasetAdapter.readDataset(new File("testrsc/car.arff")));
		MLPlan<IWekaClassifier> mlplan = new MLPlanWekaBuilder().withDataset(data).build();
		mlplan.setLoggerName("testedalgorithm");
		IGraphGenerator<TFDNode, String> graphGenerator = mlplan.getSearchProblemInputGenerator().getGraphGenerator();

		/* generate the actual input for the test */
		List<Pair<IGraphGenerator<TFDNode, String>, Integer>> gg = new ArrayList<>();
		gg.add(new Pair<>(graphGenerator, 10000));
		return gg;
	}

}
