package de.upb.crc901.mlplan.test;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaMLPlanWekaClassifier;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.GraphGeneratorTester;
import jaicore.search.core.interfaces.GraphGenerator;
import weka.core.Instances;

public class MLPlanGraphGeneratorTest extends GraphGeneratorTester<TFDNode, String> {

	@Override
	public List<Pair<GraphGenerator<TFDNode, String>,Integer>> getGraphGenerators() throws IOException {

		/* extract graph generator from mlplan */
		MLPlanWekaClassifier mlplan = new WekaMLPlanWekaClassifier();
		Instances data = new Instances(new FileReader("../../../../datasets/classification/multi-class/car.arff"));
		data.setClassIndex(data.numAttributes() - 1);
		mlplan.setData(data);
		GraphGenerator<TFDNode, String> graphGenerator = mlplan.getGraphGenerator();
		
		/* generate the actual input for the test */
		List<Pair<GraphGenerator<TFDNode, String>,Integer>> gg = new ArrayList<>();
		gg.add(new Pair<>(graphGenerator, 10000));
		return gg;
	}

}
