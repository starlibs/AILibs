package ai.libs.automl.mlplan.test;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.GraphGeneratorTester;
import ai.libs.jaicore.search.core.interfaces.GraphGenerator;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.MLPlan;
import weka.core.Instances;

public class MLPlanGraphGeneratorTest extends GraphGeneratorTester<TFDNode, String> {

	@Override
	public List<Pair<GraphGenerator<TFDNode, String>, Integer>> getGraphGenerators() throws IOException {

		/* extract graph generator from mlplan */
		Instances data = new Instances(new FileReader("testrsc/car.arff"));
		data.setClassIndex(data.numAttributes() - 1);
		MLPlan mlplan = new MLPlan(AbstractMLPlanBuilder.forWeka(), data);
		GraphGenerator<TFDNode, String> graphGenerator = mlplan.getGraphGenerator();

		/* generate the actual input for the test */
		List<Pair<GraphGenerator<TFDNode, String>, Integer>> gg = new ArrayList<>();
		gg.add(new Pair<>(graphGenerator, 10000));
		return gg;
	}

}
