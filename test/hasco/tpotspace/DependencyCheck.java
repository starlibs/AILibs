package hasco.tpotspace;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import hasco.core.HASCOFD;
import hasco.core.Solution;
import hasco.serialization.ComponentLoader;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.order.PartialOrderedSet;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.structure.core.Node;
import weka.classifiers.Classifier;

public class DependencyCheck {

	@Test
	public void test() throws IOException {
		ComponentLoader cl = new ComponentLoader();
		cl.loadComponents(new File("testrsc/autoweka.json"));
		
		/* create algorithm */
		HASCOFD<Classifier> hasco = new HASCOFD<Classifier>(n -> null, n -> 0.0, cl.getParamConfigs(), "classifier", n -> null);

		hasco.addComponents(cl.getComponents());
//		cl.getComponents().forEach(c -> {
//			System.out.println(c.getName());
//			c.getParameters().forEach(p -> System.out.println("\t" + p.getName()));
//		});
		
		new SimpleGraphVisualizationWindow<Node<TFDNode, String>>(hasco).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());
		for (Solution<ForwardDecompositionSolution,Classifier,Double> candidate : hasco) {
			System.out.println(candidate);
		}
		System.out.println("Ready");
		while (true)
			;
	}

}
